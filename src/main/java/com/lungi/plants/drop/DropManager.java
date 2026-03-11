package com.lungi.plants.drop;

import com.lungi.plants.PlantsPlugin;
import com.lungi.plants.api.RolesAPIBridge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Handles drops when a custom Lungi plant is harvested.
 */
public class DropManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final PlantsPlugin plugin;

    public DropManager(PlantsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player harvests a mature custom plant.
     * Determines drop amount based on farmer level, executes balance command,
     * and broadcasts chat notification.
     */
    public void handleHarvest(Player player) {
        RolesAPIBridge bridge = plugin.getRolesAPIBridge();
        int farmerLevel = bridge.getFarmerLevel(player);
        int amount = calculateDropAmount(farmerLevel);

        // Execute balance command
        String command = plugin.getConfig().getString("balance-command", "eco give {player} {amount}");
        command = command.replace("{player}", player.getName())
                .replace("{amount}", String.valueOf(amount));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

        // Add farmer experience
        bridge.addFarmerExp(player, 100);

        // Broadcast notification
        String message = plugin.getConfig().getString("harvest-message",
                "<green>[Plants] <yellow>{player} <green>зібрав врожай і отримав <gold>{amount} піратів!");
        message = message.replace("{player}", player.getName())
                .replace("{amount}", String.valueOf(amount));

        Component component = MM.deserialize(toLegacyMM(message));
        Bukkit.broadcast(component);
    }

    /**
     * Calculate drop amount based on farmer level.
     */
    private int calculateDropAmount(int farmerLevel) {
        if (farmerLevel >= 40) {
            return plugin.getConfig().getInt("drops.level-40-plus", 5);
        } else if (farmerLevel >= 30) {
            return plugin.getConfig().getInt("drops.level-30-39", 4);
        } else if (farmerLevel >= 20) {
            return plugin.getConfig().getInt("drops.level-20-29", 3);
        } else if (farmerLevel >= 10) {
            return plugin.getConfig().getInt("drops.level-10-19", 2);
        } else {
            return plugin.getConfig().getInt("drops.level-0-9", 1);
        }
    }

    private String toLegacyMM(String s) {
        return s.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&r", "<reset>");
    }
}
