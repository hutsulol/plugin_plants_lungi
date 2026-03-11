package com.lungi.plants.listener;

import com.lungi.plants.PlantsPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Pushes the resource pack to players on join.
 * Configure in config.yml under "resource-pack".
 */
public class ResourcePackListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final PlantsPlugin plugin;

    public ResourcePackListener(PlantsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("resource-pack.enabled", false)) return;
        String url = plugin.getConfig().getString("resource-pack.url", "").trim();
        if (url.isEmpty()) return;

        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            try {
                boolean required = plugin.getConfig().getBoolean("resource-pack.required", false);
                String prompt = plugin.getConfig().getString("resource-pack.prompt",
                        "Встановіть текстурний пак для кращого досвіду!");
                // Use Bukkit API (available in all Paper versions)
                player.setResourcePack(url, null, required,
                        MM.deserialize("<yellow>" + prompt));
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send resource pack to "
                        + player.getName() + ": " + e.getMessage());
            }
        }, 20L);
    }
}
