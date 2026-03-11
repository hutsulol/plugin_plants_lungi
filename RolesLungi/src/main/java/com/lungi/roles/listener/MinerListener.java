package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Tracks miner actions: breaking ores gives exp.
 * Miner passives:
 * - Level 5: bonus ore drops (luck)
 * - Level 10: 20% auto-smelt with fortune compatibility
 */
public class MinerListener implements Listener {

    private static final Map<Material, String> ORE_EXP_KEYS = new HashMap<>();
    private static final Map<Material, Material> SMELT_RESULTS = new HashMap<>();

    static {
        ORE_EXP_KEYS.put(Material.COAL_ORE, "miner-coal-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_COAL_ORE, "miner-coal-ore");
        ORE_EXP_KEYS.put(Material.IRON_ORE, "miner-iron-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_IRON_ORE, "miner-iron-ore");
        ORE_EXP_KEYS.put(Material.GOLD_ORE, "miner-gold-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_GOLD_ORE, "miner-gold-ore");
        ORE_EXP_KEYS.put(Material.NETHER_GOLD_ORE, "miner-gold-ore");
        ORE_EXP_KEYS.put(Material.DIAMOND_ORE, "miner-diamond-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_DIAMOND_ORE, "miner-diamond-ore");
        ORE_EXP_KEYS.put(Material.EMERALD_ORE, "miner-emerald-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_EMERALD_ORE, "miner-emerald-ore");
        ORE_EXP_KEYS.put(Material.LAPIS_ORE, "miner-lapis-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_LAPIS_ORE, "miner-lapis-ore");
        ORE_EXP_KEYS.put(Material.REDSTONE_ORE, "miner-redstone-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_REDSTONE_ORE, "miner-redstone-ore");
        ORE_EXP_KEYS.put(Material.COPPER_ORE, "miner-copper-ore");
        ORE_EXP_KEYS.put(Material.DEEPSLATE_COPPER_ORE, "miner-copper-ore");
        ORE_EXP_KEYS.put(Material.NETHER_QUARTZ_ORE, "miner-nether-quartz-ore");
        ORE_EXP_KEYS.put(Material.ANCIENT_DEBRIS, "miner-ancient-debris");

        SMELT_RESULTS.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);
        SMELT_RESULTS.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);
        SMELT_RESULTS.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        SMELT_RESULTS.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_RESULTS.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);
        SMELT_RESULTS.put(Material.COAL_ORE, Material.COAL);
        SMELT_RESULTS.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
        SMELT_RESULTS.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP);
    }

    private final RolesPlugin plugin;
    private final Random random = new Random();

    public MinerListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOreBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        String expKey = ORE_EXP_KEYS.get(block.getType());
        if (expKey == null) return; // Not an ore

        int expGain = plugin.getConfig().getInt("exp-values." + expKey, 1);
        plugin.getLevelManager().addExp(player, RoleType.MINER, expGain);

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());

        // Miner level 5: bonus drops (extra ore with configurable chance)
        if (data.isMinerBonusDropUnlocked()) {
            applyBonusDrop(player, block, event);
        }

        // Miner level 10: auto-smelt
        if (data.isMinerAutoSmeltUnlocked()) {
            applyAutoSmelt(player, block, event);
        }
    }

    private void applyBonusDrop(Player player, Block block, BlockBreakEvent event) {
        // 10% base chance for bonus ore drop (scales with level)
        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        int level = data.getRoleLevel(RoleType.MINER);
        double chance = Math.min(30.0, 10.0 + (level - 5) * 0.5);

        if (random.nextDouble() * 100 < chance) {
            // Drop extra raw ore
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
        }
    }

    private void applyAutoSmelt(Player player, Block block, BlockBreakEvent event) {
        double smeltChance = plugin.getConfig().getDouble("miner-smelt-chance", 20.0);
        if (random.nextDouble() * 100 >= smeltChance) return;

        Material smeltResult = SMELT_RESULTS.get(block.getType());
        if (smeltResult == null) return;

        // Cancel default drops and replace with smelted result
        event.setDropItems(false);

        // Calculate fortune multiplier
        int fortuneLevel = 0;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null) {
            fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);
        }

        // Base amount = 1, fortune can multiply
        int amount = 1 + (fortuneLevel > 0 ? random.nextInt(fortuneLevel + 1) : 0);

        // Special case: nether gold ore gives nuggets (9 per smelted ingot equivalent)
        if (block.getType() == Material.NETHER_GOLD_ORE) {
            amount = amount * (2 + random.nextInt(5)); // 2-6 nuggets, multiplied by fortune
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltResult, Math.max(1, amount)));
    }
}
