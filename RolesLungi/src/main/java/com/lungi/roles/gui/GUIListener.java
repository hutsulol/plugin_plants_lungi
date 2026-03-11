package com.lungi.roles.gui;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.role.RoleType;
import com.lungi.roles.skill.SkillType;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all GUI inventory click events.
 */
public class GUIListener implements Listener {

    private final RolesPlugin plugin;
    // Tracks which GUI each player has open: player UUID -> gui type
    private final Map<UUID, String> openGUIs = new HashMap<>();

    public GUIListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    public void setOpen(Player player, String guiType) {
        openGUIs.put(player.getUniqueId(), guiType);
    }

    public void clearOpen(Player player) {
        openGUIs.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            clearOpen(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inv = event.getInventory();
        String title = PlainTextComponentSerializer.plainText().serialize(inv.title());

        if (title.contains("Ролі та Прогрес")) {
            event.setCancelled(true);
            handleMainGUI(player, event.getSlot(), inv);
        } else if (title.contains("Навики")) {
            event.setCancelled(true);
            handleSkillsGUI(player, event.getSlot(), inv);
        }
    }

    private void handleMainGUI(Player player, int slot, Inventory inv) {
        // Skill button slot 22
        if (slot == 22) {
            Inventory skillsInv = SkillsGUI.build(player, plugin);
            player.openInventory(skillsInv);
            return;
        }

        // Role slots: 10-15
        int[] roleSlots = {10, 11, 12, 13, 14, 15};
        RoleType[] roles = RoleType.values();
        for (int i = 0; i < roleSlots.length; i++) {
            if (slot == roleSlots[i] && i < roles.length) {
                // Open role detail (simplified - shows info in chat for now)
                showRoleInfo(player, roles[i]);
                return;
            }
        }
    }

    private void handleSkillsGUI(Player player, int slot, Inventory inv) {
        switch (slot) {
            case SkillsGUI.VITALITY_SLOT -> {
                plugin.getSkillManager().upgradeSkill(player, SkillType.VITALITY);
                refreshSkillsGUI(player, inv);
            }
            case SkillsGUI.DAMAGE_SLOT -> {
                plugin.getSkillManager().upgradeSkill(player, SkillType.DAMAGE);
                refreshSkillsGUI(player, inv);
            }
            case SkillsGUI.DEFENSE_SLOT -> {
                plugin.getSkillManager().upgradeSkill(player, SkillType.DEFENSE);
                refreshSkillsGUI(player, inv);
            }
            case SkillsGUI.KNIGHT_CRIT_SLOT -> {
                plugin.getSkillManager().activateKnightCrit(player);
                refreshSkillsGUI(player, inv);
            }
            case SkillsGUI.BACK_SLOT -> {
                Inventory mainInv = MainGUI.build(player, plugin);
                player.openInventory(mainInv);
            }
        }
    }

    private void refreshSkillsGUI(Player player, Inventory inv) {
        Inventory newInv = SkillsGUI.build(player, plugin);
        // Update all items in the existing inventory
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, newInv.getItem(i));
        }
    }

    private void showRoleInfo(Player player, RoleType role) {
        var data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        int level = data.getRoleLevel(role);
        long exp = data.getRoleExp(role);
        long progress = plugin.getLevelManager().getExpProgress(exp);
        int expPerLevel = plugin.getConfig().getInt("exp-per-level", 1000);

        player.sendMessage("§6§l--- " + role.getDisplayName() + " ---");
        player.sendMessage("§7Рівень: §e" + level);
        player.sendMessage("§7Досвід: §e" + exp + " §7(§a" + progress + "§7/§a" + expPerLevel + "§7 до наступного)");

        // Show role-specific passives
        switch (role) {
            case FARMER -> {
                boolean unlocked = data.isFarmerSkillUnlocked();
                player.sendMessage("§7Пасивний навик [рівень 5]: §" + (unlocked ? "a✔ " : "c✗ ") + "Шанс бонусного врожаю");
                if (unlocked) {
                    int bonusLevel = plugin.getConfig().getInt("farmer-bonus-base-level", 5);
                    double chance = Math.min(30.0,
                            plugin.getConfig().getDouble("farmer-bonus-base-chance", 5.0) +
                            (level - bonusLevel) * plugin.getConfig().getDouble("farmer-bonus-per-level", 0.5));
                    player.sendMessage("  §7Поточний шанс: §e" + String.format("%.1f", chance) + "%");
                }
            }
            case MINER -> {
                player.sendMessage("§7Пасивний навик [рівень 5]: §" + (data.isMinerBonusDropUnlocked() ? "a✔ " : "c✗ ") + "Бонусні руди");
                player.sendMessage("§7Пасивний навик [рівень 10]: §" + (data.isMinerAutoSmeltUnlocked() ? "a✔ " : "c✗ ") + "Авто-плавка (20%)");
            }
            case KNIGHT -> {
                player.sendMessage("§7Скілл Крит: §" + (data.isKnightCritActive() ? "a✔ Активний" : "c✗ Не активовано"));
                player.sendMessage("§7Відкрийте меню §e/roles §7→ §eНавики §7для активації");
            }
            default -> {}
        }
    }
}
