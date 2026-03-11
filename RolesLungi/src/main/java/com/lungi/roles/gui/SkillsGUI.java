package com.lungi.roles.gui;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.skill.SkillType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills menu: upgrade Vitality, Damage, Defense.
 * Also shows the Knight crit trade option.
 */
public class SkillsGUI {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    static final String GUI_TITLE = "roles_skills";

    static final int VITALITY_SLOT = 11;
    static final int DAMAGE_SLOT = 13;
    static final int DEFENSE_SLOT = 15;
    static final int KNIGHT_CRIT_SLOT = 22;
    static final int BACK_SLOT = 26;

    public static Inventory build(Player player, RolesPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 36,
                Component.text("⭐ Навики ⭐", NamedTextColor.GOLD)
                        .decoration(TextDecoration.ITALIC, false));

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());

        // Border
        for (int i = 0; i < 36; i++) {
            if (i < 9 || i >= 27 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, MainGUI.createItem(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
            }
        }

        // Skill points display
        inv.setItem(4, MainGUI.createItem(Material.EXPERIENCE_BOTTLE,
                "<yellow>Очки навиків: <gold>" + data.getSkillPoints(),
                List.of("<gray>Отримуйте очки кожні 10 рівнів",
                        "<gray>ролей: Фермер, Шахтар, Рицар")));

        // Vitality
        inv.setItem(VITALITY_SLOT, buildSkillItem(SkillType.VITALITY, data.getVitalityLevel(),
                data.getSkillPoints(), Material.RED_DYE));

        // Damage
        inv.setItem(DAMAGE_SLOT, buildSkillItem(SkillType.DAMAGE, data.getDamageLevel(),
                data.getSkillPoints(), Material.BLAZE_POWDER));

        // Defense
        inv.setItem(DEFENSE_SLOT, buildSkillItem(SkillType.DEFENSE, data.getDefenseLevel(),
                data.getSkillPoints(), Material.SHIELD));

        // Knight crit trade
        boolean critActive = data.isKnightCritActive();
        List<String> critLore = new ArrayList<>();
        if (critActive) {
            critLore.add("<red>Активовано");
            critLore.add("<gray>5% шанс нанести 200% урон");
            critLore.add("<dark_red>-1 серце назавжди");
        } else {
            critLore.add("<gray>5% шанс нанести 200% урон");
            critLore.add("<dark_red>Коштує: 1 серце НАЗАВЖДИ");
            critLore.add("<yellow>Натисніть щоб активувати");
        }
        ItemStack critItem = MainGUI.createItem(
                critActive ? Material.TOTEM_OF_UNDYING : Material.WOODEN_SWORD,
                critActive ? "<red><bold>✦ Крит Рицаря (активний)" : "<gray>✦ Крит Рицаря",
                critLore);
        inv.setItem(KNIGHT_CRIT_SLOT, critItem);

        // Back button
        inv.setItem(BACK_SLOT, MainGUI.createItem(Material.ARROW, "<gray>← Назад", List.of()));

        return inv;
    }

    private static ItemStack buildSkillItem(SkillType skill, int currentLevel, int points, Material mat) {
        boolean canUpgrade = currentLevel < skill.getMaxLevel() && points >= 1;
        String color = canUpgrade ? "<green>" : (currentLevel >= skill.getMaxLevel() ? "<gold>" : "<red>");

        List<String> lore = new ArrayList<>();
        lore.add(skill.getDescription());
        lore.add("");
        lore.add("<gray>Рівень: " + color + currentLevel + "<gray>/<yellow>" + skill.getMaxLevel());

        // Stars display
        StringBuilder stars = new StringBuilder("<gray>");
        for (int i = 0; i < skill.getMaxLevel(); i++) {
            stars.append(i < currentLevel ? "<gold>★" : "<dark_gray>★");
        }
        lore.add(stars.toString());

        lore.add("");
        if (currentLevel >= skill.getMaxLevel()) {
            lore.add("<gold>✔ Максимальний рівень!");
        } else if (canUpgrade) {
            lore.add("<green>Натисніть щоб покращити (1 очко)");
        } else {
            lore.add("<red>Недостатньо очок навиків");
        }

        return MainGUI.createItem(mat, color + "<bold>" + skill.getDisplayName(), lore);
    }
}
