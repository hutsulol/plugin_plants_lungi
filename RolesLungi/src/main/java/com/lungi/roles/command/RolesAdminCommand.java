package com.lungi.roles.command;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Admin command: /roleadmin <set|give|reset> <player> <role> [amount]
 */
public class RolesAdminCommand implements CommandExecutor {

    private final RolesPlugin plugin;

    public RolesAdminCommand(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("roles.admin")) {
            sender.sendMessage("§cНедостатньо прав.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§eВикористання: /roleadmin <set|give|reset> <гравець> <роль> [кількість]");
            sender.sendMessage("§7Ролі: FARMER, LUMBERJACK, MINER, KNIGHT, FISHER, BUILDER");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cГравець не знайдений: " + args[1]);
            return true;
        }

        RoleType role = null;
        try {
            role = RoleType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            role = RoleType.fromKey(args[2]);
        }

        if (role == null) {
            sender.sendMessage("§cНевідома роль: " + args[2]);
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(target.getUniqueId());

        switch (action) {
            case "give" -> {
                if (args.length < 4) {
                    sender.sendMessage("§eВикористання: /roleadmin give <гравець> <роль> <кількість>");
                    return true;
                }
                try {
                    long amount = Long.parseLong(args[3]);
                    plugin.getLevelManager().addExp(target, role, amount);
                    sender.sendMessage("§aВидано §e" + amount + " §aдосвіду §e" + role.getDisplayName() +
                            " §aгравцю §e" + target.getName());
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cНеправильне число: " + args[3]);
                }
            }
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage("§eВикористання: /roleadmin set <гравець> <роль> <рівень>");
                    return true;
                }
                try {
                    int level = Integer.parseInt(args[3]);
                    int expPerLevel = plugin.getConfig().getInt("exp-per-level", 1000);
                    long totalExp = (long) level * expPerLevel;
                    data.setRoleExp(role, totalExp);
                    data.setRoleLevel(role, level);
                    sender.sendMessage("§aВстановлено рівень §e" + level + " §aу ролі §e" + role.getDisplayName() +
                            " §aгравцю §e" + target.getName());
                    plugin.getSkillManager().applyEffects(target);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cНеправильне число: " + args[3]);
                }
            }
            case "reset" -> {
                data.setRoleExp(role, 0);
                data.setRoleLevel(role, 0);
                sender.sendMessage("§aСкинуто роль §e" + role.getDisplayName() +
                        " §aгравця §e" + target.getName());
                plugin.getSkillManager().applyEffects(target);
            }
            case "skillpoints" -> {
                if (args.length < 3) {
                    sender.sendMessage("§eВикористання: /roleadmin skillpoints <гравець> <кількість>");
                    return true;
                }
                try {
                    int points = Integer.parseInt(args[2]);
                    data.addSkillPoints(points);
                    sender.sendMessage("§aВидано §e" + points + " §aочок навиків гравцю §e" + target.getName() +
                            "§a. Всього: §e" + data.getSkillPoints());
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cНеправильне число: " + args[2]);
                }
            }
            default -> sender.sendMessage("§cНевідома дія: " + action + ". Використовуйте: set, give, reset, skillpoints");
        }
        return true;
    }
}
