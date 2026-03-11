package com.lungi.roles.command;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.gui.MainGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class RolesCommand implements CommandExecutor {

    private final RolesPlugin plugin;

    public RolesCommand(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЦю команду може виконати лише гравець.");
            return true;
        }

        Inventory inv = MainGUI.build(player, plugin);
        player.openInventory(inv);
        return true;
    }
}
