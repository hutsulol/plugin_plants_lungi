package com.lungi.roles.skill;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.data.PlayerDataManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Manages skill upgrades and their passive effects on players.
 */
public class SkillManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final UUID VITALITY_MOD_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID DAMAGE_MOD_UUID   = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f01234567891");

    private final RolesPlugin plugin;

    public SkillManager(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempt to upgrade a skill for the given player.
     * Returns true if successful, false if not enough points or at max level.
     */
    public boolean upgradeSkill(Player player, SkillType skill) {
        PlayerDataManager pdm = plugin.getPlayerDataManager();
        PlayerData data = pdm.getOrCreate(player.getUniqueId());

        int currentLevel = switch (skill) {
            case VITALITY -> data.getVitalityLevel();
            case DAMAGE -> data.getDamageLevel();
            case DEFENSE -> data.getDefenseLevel();
        };

        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(MM.deserialize("<red>Навик вже на максимальному рівні!"));
            return false;
        }

        if (data.getSkillPoints() < 1) {
            player.sendMessage(MM.deserialize("<red>Недостатньо очок навиків!"));
            return false;
        }

        data.addSkillPoints(-1);
        int newLevel = currentLevel + 1;

        switch (skill) {
            case VITALITY -> data.setVitalityLevel(newLevel);
            case DAMAGE -> data.setDamageLevel(newLevel);
            case DEFENSE -> data.setDefenseLevel(newLevel);
        }

        applyEffects(player);

        String msg = plugin.getConfig().getString("messages.skill-upgraded",
                "<green>[Ролі] <yellow>Навик <aqua>{skill} <yellow>покращено до рівня <red>{level}!");
        msg = msg.replace("{skill}", skill.getDisplayName()).replace("{level}", String.valueOf(newLevel));
        player.sendMessage(MM.deserialize(msg));
        return true;
    }

    /**
     * Activates the Knight crit skill (trades 1 heart for 5% crit).
     */
    public boolean activateKnightCrit(Player player) {
        PlayerDataManager pdm = plugin.getPlayerDataManager();
        PlayerData data = pdm.getOrCreate(player.getUniqueId());

        if (data.isKnightCritActive()) {
            player.sendMessage(MM.deserialize("<red>Критичний удар вже активований!"));
            return false;
        }

        // Deduct 1 heart (2 HP)
        var maxHpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHpAttr == null || maxHpAttr.getValue() <= 2) {
            player.sendMessage(MM.deserialize("<red>Недостатньо здоров'я для активації!"));
            return false;
        }

        data.setKnightCritActive(true);
        applyEffects(player);

        String msg = plugin.getConfig().getString("messages.knight-crit-warning",
                "<red>[Рицар] <yellow>Ви активували критичний удар! <red>-1 серце навсегда.");
        player.sendMessage(MM.deserialize(msg));
        return true;
    }

    /**
     * Apply all passive skill effects to a player (called on join and skill upgrade).
     */
    public void applyEffects(Player player) {
        PlayerDataManager pdm = plugin.getPlayerDataManager();
        PlayerData data = pdm.getOrCreate(player.getUniqueId());

        applyVitality(player, data);
        applyDamage(player, data);
        applyDefense(player, data);
        applyKnightCrit(player, data);
    }

    private void applyVitality(Player player, PlayerData data) {
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        // Remove old modifier
        attr.removeModifier(new AttributeModifier(VITALITY_MOD_UUID, 0,
                AttributeModifier.Operation.ADD_NUMBER));

        // Calculate total HP adjustment
        double hpBonus = data.getVitalityLevel() * 2.0; // +2 HP per level = 1 heart
        double hpPenalty = data.isKnightCritActive() ? -2.0 : 0.0; // -1 heart if crit active

        double totalMod = hpBonus + hpPenalty;
        if (totalMod != 0) {
            AttributeModifier mod = new AttributeModifier(VITALITY_MOD_UUID, totalMod,
                    AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(mod);
        }

        // Ensure current health doesn't exceed new max
        if (player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }

    private void applyDamage(Player player, PlayerData data) {
        var attr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attr == null) return;

        attr.removeModifier(new AttributeModifier(DAMAGE_MOD_UUID, 0,
                AttributeModifier.Operation.ADD_NUMBER));

        double bonus = data.getDamageLevel() * 0.5;
        if (bonus > 0) {
            AttributeModifier mod = new AttributeModifier(DAMAGE_MOD_UUID, bonus,
                    AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(mod);
        }
    }

    private void applyDefense(Player player, PlayerData data) {
        // Remove existing resistance effects applied by this plugin
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        int defLevel = data.getDefenseLevel();
        if (defLevel <= 0) return;

        // Resistance amplifier: 0 = Resistance I, 1 = Resistance II, 2 = Resistance III
        int amplifier = Math.min(defLevel - 1, 2);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                Integer.MAX_VALUE,
                amplifier,
                false,
                false,  // hide particles
                true    // show icon
        ));
    }

    private void applyKnightCrit(Player player, PlayerData data) {
        // The crit penalty (-1 heart) is applied via applyVitality
        // Actual crit logic is in KnightListener
    }

    /**
     * Remove all plugin-applied effects (called on quit to prevent duplication).
     */
    public void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        var hpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (hpAttr != null) {
            hpAttr.removeModifier(new AttributeModifier(VITALITY_MOD_UUID, 0,
                    AttributeModifier.Operation.ADD_NUMBER));
        }
        var dmgAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.removeModifier(new AttributeModifier(DAMAGE_MOD_UUID, 0,
                    AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
