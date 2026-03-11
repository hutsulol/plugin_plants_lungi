package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Tracks farmer actions: harvesting mature crops gives 1 exp.
 * Farmer passive: bonus crop drops based on level.
 */
public class FarmerListener implements Listener {

    private static final Set<Material> HARVESTABLE_CROPS = EnumSet.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.NETHER_WART, Material.COCOA, Material.MELON, Material.PUMPKIN,
            Material.SWEET_BERRY_BUSH, Material.SUGAR_CANE, Material.BAMBOO,
            Material.CACTUS, Material.PITCHER_CROP, Material.TORCHFLOWER_CROP
    );

    private final RolesPlugin plugin;
    private final Random random = new Random();

    public FarmerListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCropHarvest(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!HARVESTABLE_CROPS.contains(block.getType())) return;

        // Only reward for mature crops (age == max)
        if (block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() < ageable.getMaximumAge()) return;
        }

        int expGain = plugin.getConfig().getInt("exp-values.farmer-crop-harvest", 1);
        plugin.getLevelManager().addExp(player, RoleType.FARMER, expGain);

        // Apply farmer bonus drops
        applyBonusDrop(player, block, event);
    }

    private void applyBonusDrop(Player player, Block block, BlockBreakEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        if (!data.isFarmerSkillUnlocked()) return;

        int level = data.getRoleLevel(RoleType.FARMER);
        double baseChance = plugin.getConfig().getDouble("farmer-bonus-base-chance", 5.0);
        double perLevel = plugin.getConfig().getDouble("farmer-bonus-per-level", 0.5);
        int baseLevel = plugin.getConfig().getInt("farmer-bonus-base-level", 5);
        double maxChance = plugin.getConfig().getDouble("farmer-bonus-max-chance", 30.0);

        double chance = Math.min(maxChance, baseChance + (level - baseLevel) * perLevel);

        if (random.nextDouble() * 100 < chance) {
            // Drop a bonus item (same as the crop's natural drop)
            ItemStack bonus = getBonusDrop(block.getType());
            if (bonus != null) {
                block.getWorld().dropItemNaturally(block.getLocation(), bonus);
            }
        }
    }

    private ItemStack getBonusDrop(Material cropType) {
        return switch (cropType) {
            case WHEAT -> new ItemStack(Material.WHEAT);
            case CARROTS -> new ItemStack(Material.CARROT);
            case POTATOES -> new ItemStack(Material.POTATO);
            case BEETROOTS -> new ItemStack(Material.BEETROOT);
            case NETHER_WART -> new ItemStack(Material.NETHER_WART);
            case MELON -> new ItemStack(Material.MELON_SLICE, 1 + random.nextInt(4));
            case PUMPKIN -> new ItemStack(Material.PUMPKIN);
            case SUGAR_CANE -> new ItemStack(Material.SUGAR_CANE);
            case SWEET_BERRY_BUSH -> new ItemStack(Material.SWEET_BERRIES);
            default -> null;
        };
    }
}
