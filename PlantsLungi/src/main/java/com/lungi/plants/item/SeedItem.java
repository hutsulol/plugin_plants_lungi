package com.lungi.plants.item;

import com.lungi.plants.PlantsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Factory for creating the custom Lungi plant seed item.
 */
public class SeedItem {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static ItemStack create(int amount) {
        PlantsPlugin plugin = PlantsPlugin.getInstance();
        int customModelData = plugin.getConfig().getInt("seed-custom-model-data", 1001);
        String name = plugin.getConfig().getString("seed-name", "<green>Насіння Лунгі");
        List<String> loreStrings = plugin.getConfig().getStringList("seed-lore");

        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, amount);
        ItemMeta meta = item.getItemMeta();

        // Convert legacy & color codes to adventure component
        meta.displayName(MM.deserialize(toLegacyMM(name)));

        List<Component> lore = loreStrings.stream()
                .map(l -> MM.deserialize(toLegacyMM(l)))
                .toList();
        meta.lore(lore);

        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Converts legacy & color codes to MiniMessage format.
     */
    private static String toLegacyMM(String s) {
        // Basic conversion of common & codes
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
                .replace("&o", "<italic>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>")
                .replace("&r", "<reset>");
    }

    /**
     * Checks if an ItemStack is the custom Lungi seed.
     */
    public static boolean isLungiSeed(ItemStack item) {
        if (item == null || item.getType() != Material.WHEAT_SEEDS) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        int customModelData = PlantsPlugin.getInstance().getConfig().getInt("seed-custom-model-data", 1001);
        return meta.hasCustomModelData() && meta.getCustomModelData() == customModelData;
    }
}
