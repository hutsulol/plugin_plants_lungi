package com.lungi.roles.level;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.data.PlayerDataManager;
import com.lungi.roles.role.RoleType;
import com.lungi.roles.skill.SkillManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/**
 * Manages experience gain and level-up logic for all roles.
 */
public class LevelManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final RolesPlugin plugin;
    private final int expPerLevel;
    private final int skillPointInterval;

    // Roles that award skill points on level-up every N levels
    private static final Set<RoleType> SKILL_POINT_ROLES = Set.of(
            RoleType.FARMER, RoleType.MINER, RoleType.KNIGHT
    );

    public LevelManager(RolesPlugin plugin) {
        this.plugin = plugin;
        this.expPerLevel = plugin.getConfig().getInt("exp-per-level", 1000);
        this.skillPointInterval = plugin.getConfig().getInt("skill-point-interval", 10);
    }

    /**
     * Add experience to a player's role and trigger level-up if needed.
     */
    public void addExp(Player player, RoleType role, long amount) {
        PlayerDataManager pdm = plugin.getPlayerDataManager();
        PlayerData data = pdm.getOrCreate(player.getUniqueId());

        data.addRoleExp(role, amount);

        // Check for level-up(s)
        int currentLevel = data.getRoleLevel(role);
        long totalExp = data.getRoleExp(role);

        int newLevel = calculateLevel(totalExp);
        if (newLevel > currentLevel) {
            data.setRoleLevel(role, newLevel);
            for (int lvl = currentLevel + 1; lvl <= newLevel; lvl++) {
                onLevelUp(player, data, role, lvl);
            }
        }

        // Unlock passive skills based on new level
        checkPassiveUnlocks(player, data, role);
    }

    /**
     * Calculate level from total exp (1000 exp per level flat).
     */
    public int calculateLevel(long totalExp) {
        return (int) (totalExp / expPerLevel);
    }

    /**
     * Get exp progress toward next level (0 to expPerLevel-1).
     */
    public long getExpProgress(long totalExp) {
        return totalExp % expPerLevel;
    }

    /**
     * Called when a player gains a new level in a role.
     */
    private void onLevelUp(Player player, PlayerData data, RoleType role, int newLevel) {
        // Announce
        String msg = plugin.getConfig().getString("messages.level-up",
                "<gold>[Ролі] <yellow>{player} <gold>підвищив рівень ролі <aqua>{role} <gold>до рівня <red>{level}!");
        msg = msg.replace("{player}", player.getName())
                .replace("{role}", role.getDisplayName())
                .replace("{level}", String.valueOf(newLevel));
        Bukkit.broadcast(MM.deserialize(msg));

        // Award skill points for eligible roles every N levels
        if (SKILL_POINT_ROLES.contains(role) && newLevel % skillPointInterval == 0) {
            data.addSkillPoints(1);
            String spMsg = plugin.getConfig().getString("messages.skill-point-gained",
                    "<gold>[Ролі] <yellow>Ви отримали очко навику! Всього: <red>{points}");
            spMsg = spMsg.replace("{points}", String.valueOf(data.getSkillPoints()));
            player.sendMessage(MM.deserialize(spMsg));
        }

        // Award milestone rewards for non-skill-point roles
        if (!SKILL_POINT_ROLES.contains(role)) {
            checkMilestoneReward(player, data, role, newLevel);
        }
    }

    private void checkMilestoneReward(Player player, PlayerData data, RoleType role, int level) {
        String configKey = switch (role) {
            case LUMBERJACK -> "lumberjack-milestones." + level;
            case FISHER -> "fisher-milestones." + level;
            case BUILDER -> "builder-milestones." + level;
            default -> null;
        };

        if (configKey == null) return;
        int emeralds = plugin.getConfig().getInt(configKey, 0);
        if (emeralds <= 0) return;

        if (!data.hasMilestoneClaimed(role, level)) {
            data.claimMilestone(role, level);
            player.getInventory().addItem(new ItemStack(Material.EMERALD, emeralds));

            String msg = plugin.getConfig().getString("messages.milestone-reward",
                    "<gold>[Ролі] <yellow>Ви досягли рівня <red>{level} <yellow>у ролі <aqua>{role}<yellow>! Нагорода: <green>{reward}");
            msg = msg.replace("{level}", String.valueOf(level))
                    .replace("{role}", role.getDisplayName())
                    .replace("{reward}", emeralds + "x Смарагд");
            player.sendMessage(MM.deserialize(msg));
        }
    }

    private void checkPassiveUnlocks(Player player, PlayerData data, RoleType role) {
        int level = data.getRoleLevel(role);

        if (role == RoleType.FARMER) {
            int baseLevel = plugin.getConfig().getInt("farmer-bonus-base-level", 5);
            if (level >= baseLevel && !data.isFarmerSkillUnlocked()) {
                data.setFarmerSkillUnlocked(true);
                player.sendMessage(MM.deserialize(
                        "<green>[Фермер] <yellow>Пасивний навик <aqua>Фермер <yellow>розблоковано! " +
                        "Шанс на бонусний врожай при збиранні культур."));
            }
        }

        if (role == RoleType.MINER) {
            if (level >= 5 && !data.isMinerBonusDropUnlocked()) {
                data.setMinerBonusDropUnlocked(true);
                player.sendMessage(MM.deserialize(
                        "<green>[Шахтар] <yellow>Пасивний навик <aqua>Удача Шахтаря <yellow>розблоковано! " +
                        "Шанс на додаткові ресурси з руд."));
            }
            if (level >= 10 && !data.isMinerAutoSmeltUnlocked()) {
                data.setMinerAutoSmeltUnlocked(true);
                player.sendMessage(MM.deserialize(
                        "<green>[Шахтар] <yellow>Пасивний навик <aqua>Авто-Плавка <yellow>розблоковано! " +
                        "20% шанс що руда буде одразу переплавлена."));
            }
        }
    }
}
