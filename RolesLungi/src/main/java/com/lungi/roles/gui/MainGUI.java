package com.lungi.roles.gui;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.level.LevelManager;
import com.lungi.roles.role.RoleType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Main roles menu - shows all 6 roles with level/exp info.
 * 9x4 inventory:
 * Row 1-2: Role buttons (6 roles)
 * Row 3: Skills button, empty space
 * Row 4: Border
 */
public class MainGUI {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    static final String GUI_TITLE = "roles_main";

    // Slot layout
    private static final int[] ROLE_SLOTS = {10, 11, 12, 13, 14, 15};
    private static final int SKILLS_SLOT = 22;

    public static Inventory build(Player player, RolesPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 36,
                Component.text("⚔ Ролі та Прогрес ⚔", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        LevelManager lm = plugin.getLevelManager();

        // Fill border
        fillBorder(inv, Material.BLACK_STAINED_GLASS_PANE);

        // Role buttons
        RoleType[] roles = RoleType.values();
        for (int i = 0; i < roles.length && i < ROLE_SLOTS.length; i++) {
            RoleType role = roles[i];
            int level = data.getRoleLevel(role);
            long totalExp = data.getRoleExp(role);
            long progress = lm.getExpProgress(totalExp);
            int expPerLevel = plugin.getConfig().getInt("exp-per-level", 1000);

            ItemStack item = buildRoleItem(role, level, progress, expPerLevel);
            inv.setItem(ROLE_SLOTS[i], item);
        }

        // Skills button
        ItemStack skillsBtn = createItem(Material.NETHER_STAR,
                "<gold><bold>Навики",
                List.of(
                        "<gray>Очки навиків: <yellow>" + data.getSkillPoints(),
                        "<gray>Живучість: <green>" + data.getVitalityLevel() + "/5",
                        "<gray>Урон: <red>" + data.getDamageLevel() + "/5",
                        "<gray>Захист: <aqua>" + data.getDefenseLevel() + "/3",
                        "",
                        "<yellow>Натисніть щоб відкрити"
                ));
        inv.setItem(SKILLS_SLOT, skillsBtn);

        return inv;
    }

    static ItemStack buildRoleItem(RoleType role, int level, long progress, int expPerLevel) {
        Material mat = getRoleMaterial(role);
        List<String> lore = new ArrayList<>();
        lore.add("<gray>Рівень: <gold>" + level);
        lore.add("<gray>Прогрес: <yellow>" + progress + "<gray>/<yellow>" + expPerLevel);

        // Progress bar
        lore.add(buildProgressBar(progress, expPerLevel));
        lore.add("");
        lore.add("<yellow>Натисніть для деталей");

        return createItem(mat, "<aqua><bold>" + role.getIcon() + " " + role.getDisplayName(), lore);
    }

    private static String buildProgressBar(long current, long max) {
        int bars = 20;
        int filled = (int) Math.round((double) current / max * bars);
        StringBuilder sb = new StringBuilder("<dark_gray>[");
        for (int i = 0; i < bars; i++) {
            sb.append(i < filled ? "<green>|" : "<dark_green>|");
        }
        sb.append("<dark_gray>]");
        return sb.toString();
    }

    private static void fillBorder(Inventory inv, Material mat) {
        ItemStack glass = createItem(mat, " ", List.of());
        int size = inv.getSize();
        int rows = size / 9;
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        for (int i = size - 9; i < size; i++) inv.setItem(i, glass);
        for (int row = 1; row < rows - 1; row++) {
            inv.setItem(row * 9, glass);
            inv.setItem(row * 9 + 8, glass);
        }
    }

    static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MM.deserialize(name).decoration(TextDecoration.ITALIC, false));
        List<Component> loreComponents = lore.stream()
                .map(l -> MM.deserialize(l).decoration(TextDecoration.ITALIC, false))
                .toList();
        meta.lore(loreComponents);
        item.setItemMeta(meta);
        return item;
    }

    private static Material getRoleMaterial(RoleType role) {
        return switch (role) {
            case FARMER -> Material.WHEAT;
            case LUMBERJACK -> Material.OAK_AXE;
            case MINER -> Material.DIAMOND_PICKAXE;
            case KNIGHT -> Material.DIAMOND_SWORD;
            case FISHER -> Material.FISHING_ROD;
            case BUILDER -> Material.BRICKS;
        };
    }
}
