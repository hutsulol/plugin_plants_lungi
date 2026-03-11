package com.lungi.roles.skill;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.data.PlayerDataManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Manages skill upgrades and their passive effects on players.
 * Uses NamespacedKey-based AttributeModifiers (Paper 1.21.4 API).
 */
public class SkillManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final RolesPlugin plugin;
    private final NamespacedKey vitalityKey;
    private final NamespacedKey damageKey;

    public SkillManager(RolesPlugin plugin) {
        this.plugin = plugin;
        this.vitalityKey = new NamespacedKey(plugin, "vitality_bonus");
        this.damageKey   = new NamespacedKey(plugin, "damage_bonus");
    }

    /**
     * Attempt to upgrade a skill for the given player.
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
    }

    private void applyVitality(Player player, PlayerData data) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        // Remove existing modifier by key
        attr.removeModifier(vitalityKey);

        double hpBonus = data.getVitalityLevel() * 2.0;           // +2 HP per level = 1 heart
        double hpPenalty = data.isKnightCritActive() ? -2.0 : 0.0; // -1 heart if crit active
        double totalMod = hpBonus + hpPenalty;

        if (totalMod != 0) {
            attr.addModifier(new AttributeModifier(vitalityKey, totalMod,
                    AttributeModifier.Operation.ADD_NUMBER));
        }

        if (player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }

    private void applyDamage(Player player, PlayerData data) {
        AttributeInstance attr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (attr == null) return;

        attr.removeModifier(damageKey);

        double bonus = data.getDamageLevel() * 0.5;
        if (bonus > 0) {
            attr.addModifier(new AttributeModifier(damageKey, bonus,
                    AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    private void applyDefense(Player player, PlayerData data) {
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        int defLevel = data.getDefenseLevel();
        if (defLevel <= 0) return;

        int amplifier = Math.min(defLevel - 1, 2); // 0=Resistance I, 1=II, 2=III
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE,
                Integer.MAX_VALUE,
                amplifier,
                false,
                false,
                true
        ));
    }

    public void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.RESISTANCE);

        AttributeInstance hpAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (hpAttr != null) hpAttr.removeModifier(vitalityKey);

        AttributeInstance dmgAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dmgAttr != null) dmgAttr.removeModifier(damageKey);
    }
}
