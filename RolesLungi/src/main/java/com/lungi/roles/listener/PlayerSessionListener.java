package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit to apply/remove persistent skill effects and boss bars.
 */
public class PlayerSessionListener implements Listener {

    private final RolesPlugin plugin;

    public PlayerSessionListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        // Slight delay for attribute sync
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            plugin.getSkillManager().applyEffects(player);
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getBossBarManager().cleanup(event.getPlayer());
    }
}
