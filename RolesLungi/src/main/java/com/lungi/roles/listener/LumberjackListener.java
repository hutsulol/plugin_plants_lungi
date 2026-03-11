package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.role.RoleType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tracks lumberjack actions: breaking log blocks.
 * Each log broken = 1 exp.
 */
public class LumberjackListener implements Listener {

    private static final Set<Material> LOG_MATERIALS = EnumSet.of(
            Material.OAK_LOG, Material.OAK_WOOD,
            Material.BIRCH_LOG, Material.BIRCH_WOOD,
            Material.SPRUCE_LOG, Material.SPRUCE_WOOD,
            Material.JUNGLE_LOG, Material.JUNGLE_WOOD,
            Material.ACACIA_LOG, Material.ACACIA_WOOD,
            Material.DARK_OAK_LOG, Material.DARK_OAK_WOOD,
            Material.MANGROVE_LOG, Material.MANGROVE_WOOD,
            Material.CHERRY_LOG, Material.CHERRY_WOOD,
            Material.BAMBOO_BLOCK,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_BIRCH_LOG,
            Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG
    );

    private final RolesPlugin plugin;

    public LumberjackListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!LOG_MATERIALS.contains(block.getType())) return;

        // Choose exp value based on log type (all logs give 1 by default from config)
        int expGain = getExpForLog(block.getType());
        plugin.getLevelManager().addExp(player, RoleType.LUMBERJACK, expGain);
    }

    private int getExpForLog(Material type) {
        String key = switch (type) {
            case OAK_LOG, OAK_WOOD, STRIPPED_OAK_LOG -> "lumberjack-oak-log";
            case BIRCH_LOG, BIRCH_WOOD, STRIPPED_BIRCH_LOG -> "lumberjack-birch-log";
            case SPRUCE_LOG, SPRUCE_WOOD, STRIPPED_SPRUCE_LOG -> "lumberjack-spruce-log";
            case JUNGLE_LOG, JUNGLE_WOOD, STRIPPED_JUNGLE_LOG -> "lumberjack-jungle-log";
            case ACACIA_LOG, ACACIA_WOOD, STRIPPED_ACACIA_LOG -> "lumberjack-acacia-log";
            case DARK_OAK_LOG, DARK_OAK_WOOD, STRIPPED_DARK_OAK_LOG -> "lumberjack-dark-oak-log";
            case MANGROVE_LOG, MANGROVE_WOOD, STRIPPED_MANGROVE_LOG -> "lumberjack-mangrove-log";
            case CHERRY_LOG, CHERRY_WOOD, STRIPPED_CHERRY_LOG -> "lumberjack-cherry-log";
            default -> "lumberjack-oak-log";
        };
        return plugin.getConfig().getInt("exp-values." + key, 1);
    }
}
