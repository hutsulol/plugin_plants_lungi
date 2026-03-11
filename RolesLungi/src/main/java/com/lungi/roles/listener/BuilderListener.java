package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.EnumSet;
import java.util.Set;

/**
 * Tracks builder actions: placing blocks gives exp (max 200/hour).
 * Excludes trivial blocks like torches, buttons, etc.
 */
public class BuilderListener implements Listener {

    // Blocks that don't count for builder exp (too easy to abuse)
    private static final Set<Material> EXCLUDED_BLOCKS = EnumSet.of(
            Material.TORCH, Material.WALL_TORCH, Material.SOUL_TORCH,
            Material.REDSTONE_TORCH, Material.LEVER,
            Material.OAK_BUTTON, Material.BIRCH_BUTTON, Material.SPRUCE_BUTTON,
            Material.JUNGLE_BUTTON, Material.ACACIA_BUTTON, Material.DARK_OAK_BUTTON,
            Material.CRIMSON_BUTTON, Material.WARPED_BUTTON, Material.STONE_BUTTON,
            Material.POLISHED_BLACKSTONE_BUTTON,
            Material.TRIPWIRE_HOOK, Material.STRING,
            Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO,
            Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS,
            Material.DIRT, Material.GRAVEL, Material.SAND, Material.AIR,
            Material.OAK_SIGN, Material.BIRCH_SIGN
    );

    private final RolesPlugin plugin;

    public BuilderListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Material placed = event.getBlock().getType();

        if (EXCLUDED_BLOCKS.contains(placed)) return;

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        int hourlyCap = plugin.getConfig().getInt("builder-hourly-cap", 200);

        // Reset hourly counter if an hour has passed
        long now = System.currentTimeMillis();
        if (now - data.getBuilderHourStart() > 3_600_000L) {
            data.setBuilderHourStart(now);
            data.setBuilderExpThisHour(0);
        }

        // Check if we've hit the hourly cap
        if (data.getBuilderExpThisHour() >= hourlyCap) return;

        int expGain = plugin.getConfig().getInt("exp-values.builder-place-block", 1);
        int remaining = hourlyCap - data.getBuilderExpThisHour();
        int actual = Math.min(expGain, remaining);

        data.setBuilderExpThisHour(data.getBuilderExpThisHour() + actual);
        plugin.getLevelManager().addExp(player, RoleType.BUILDER, actual);
    }
}
