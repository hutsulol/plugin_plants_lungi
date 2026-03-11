package com.lungi.roles.gui;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.role.RoleType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shows a boss bar with role XP progress when a player gains experience.
 * Each role gets its own bar; bars auto-hide 5 seconds after the last update.
 */
public class BossBarManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final RolesPlugin plugin;
    // playerUUID -> (role -> bar)
    private final Map<UUID, EnumMap<RoleType, BossBar>> bars = new HashMap<>();
    // playerUUID -> (role -> pending hide task)
    private final Map<UUID, EnumMap<RoleType, BukkitTask>> hideTasks = new HashMap<>();

    public BossBarManager(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Show (or update) the XP progress bar for the given role.
     * Automatically hides after 5 seconds of inactivity.
     */
    public void showProgress(Player player, RoleType role, int level, long progress, long expPerLevel) {
        UUID uuid = player.getUniqueId();
        bars.computeIfAbsent(uuid, k -> new EnumMap<>(RoleType.class));
        hideTasks.computeIfAbsent(uuid, k -> new EnumMap<>(RoleType.class));

        BossBar bar = bars.get(uuid).computeIfAbsent(role,
                r -> BossBar.bossBar(MM.deserialize(""), 1.0f, roleColor(r), BossBar.Overlay.PROGRESS));

        // Update title and progress
        float frac = expPerLevel > 0 ? (float) progress / expPerLevel : 0f;
        frac = Math.min(1f, Math.max(0f, frac));
        String color = roleColorTag(role);
        bar.name(MM.deserialize(
                "<" + color + ">" + role.getDisplayName() +
                " <white>— Рівень <yellow>" + level +
                " <gray>(" + progress + "/" + expPerLevel + " xp)"));
        bar.progress(frac);

        player.showBossBar(bar);

        // Reset hide timer
        BukkitTask old = hideTasks.get(uuid).get(role);
        if (old != null) old.cancel();
        BukkitTask hideTask = plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> player.hideBossBar(bar), 100L); // 5 seconds
        hideTasks.get(uuid).put(role, hideTask);
    }

    /** Remove all bars for a player (call on quit). */
    public void cleanup(Player player) {
        UUID uuid = player.getUniqueId();
        EnumMap<RoleType, BossBar> playerBars = bars.remove(uuid);
        if (playerBars != null) playerBars.values().forEach(b -> player.hideBossBar(b));
        EnumMap<RoleType, BukkitTask> tasks = hideTasks.remove(uuid);
        if (tasks != null) tasks.values().forEach(BukkitTask::cancel);
    }

    private BossBar.Color roleColor(RoleType role) {
        return switch (role) {
            case FARMER    -> BossBar.Color.GREEN;
            case LUMBERJACK -> BossBar.Color.YELLOW;
            case MINER     -> BossBar.Color.WHITE;
            case KNIGHT    -> BossBar.Color.RED;
            case FISHER    -> BossBar.Color.BLUE;
            case BUILDER   -> BossBar.Color.PURPLE;
        };
    }

    private String roleColorTag(RoleType role) {
        return switch (role) {
            case FARMER    -> "green";
            case LUMBERJACK -> "yellow";
            case MINER     -> "white";
            case KNIGHT    -> "red";
            case FISHER    -> "aqua";
            case BUILDER   -> "light_purple";
        };
    }
}
