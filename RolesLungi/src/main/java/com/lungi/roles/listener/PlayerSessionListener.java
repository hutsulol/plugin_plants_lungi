package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit to apply/remove persistent skill effects.
 */
public class PlayerSessionListener implements Listener {

    private final RolesPlugin plugin;

    public PlayerSessionListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Ensure data is loaded and apply effects with slight delay for attribute sync
        var player = event.getPlayer();
        plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSkillManager().applyEffects(player);
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Save player data on quit
        // Effects will be reapplied on next join, no need to remove
        // (they are attribute modifiers and potion effects that persist naturally)
    }
}
