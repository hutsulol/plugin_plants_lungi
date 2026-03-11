package com.lungi.plants.listener;

import com.lungi.plants.PlantsPlugin;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.util.UUID;

/**
 * Pushes the resource pack to players when they join.
 *
 * Configuration (config.yml):
 *   resource-pack:
 *     enabled: true
 *     url: "https://example.com/resourcepack.zip"
 *     sha256: ""        # optional but recommended
 *     required: false   # kick player if they decline
 *     prompt: "..."     # message shown in the pack prompt
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

        // Small delay so the join sequence completes first
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            try {
                ResourcePackInfo.Builder infoBuilder = ResourcePackInfo.resourcePackInfo()
                        .id(UUID.nameUUIDFromBytes(url.getBytes()))
                        .uri(URI.create(url));

                String hash = plugin.getConfig().getString("resource-pack.sha256", "").trim();
                if (!hash.isEmpty()) infoBuilder.hash(hash);

                boolean required = plugin.getConfig().getBoolean("resource-pack.required", false);
                String promptText = plugin.getConfig().getString(
                        "resource-pack.prompt", "<yellow>Встановіть текстурний пак для кращого досвіду!");

                ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                        .packs(infoBuilder.build())
                        .required(required)
                        .prompt(MM.deserialize(promptText))
                        .build();

                player.sendResourcePacks(request);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send resource pack to "
                        + player.getName() + ": " + e.getMessage());
            }
        }, 20L); // 1 second delay
    }
}
