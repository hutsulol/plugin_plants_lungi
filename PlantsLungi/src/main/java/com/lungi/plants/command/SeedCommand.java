package com.lungi.plants.command;

import com.lungi.plants.PlantsPlugin;
import com.lungi.plants.item.SeedItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SeedCommand implements CommandExecutor {

    private final PlantsPlugin plugin;

    public SeedCommand(PlantsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // /plantseed [amount] [player]
        int amount = 1;
        Player target = null;

        if (args.length >= 1) {
            try {
                amount = Math.min(64, Math.max(1, Integer.parseInt(args[0])));
            } catch (NumberFormatException e) {
                target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("§cГравець не знайдений: " + args[0]);
                    return true;
                }
            }
        }

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cГравець не знайдений: " + args[1]);
                return true;
            }
        }

        if (target == null) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage("§cВкажіть гравця: /plantseed <кількість> <гравець>");
                return true;
            }
            target = p;
        }

        ItemStack seed = SeedItem.create(amount);
        target.getInventory().addItem(seed);
        target.sendMessage("§aВи отримали §2" + amount + "x Насіння Лунгі§a!");
        if (!target.equals(sender)) {
            sender.sendMessage("§aВидано §2" + amount + "x Насіння Лунгі §aгравцю §e" + target.getName());
        }
        return true;
    }
}
