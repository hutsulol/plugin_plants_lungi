package com.lungi.plants.listener;

import com.lungi.plants.PlantsPlugin;
import com.lungi.plants.drop.DropManager;
import com.lungi.plants.item.SeedItem;
import com.lungi.plants.manager.PlantDataManager;
import com.lungi.plants.plant.GrowthStage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles all events related to the custom Lungi plant.
 *
 * Mechanics:
 * - Planting: only on FARMLAND, using the custom seed item
 * - Growth: WHEAT crops with controlled stages (0,2,4,6,7 = 5 visual stages)
 * - Breaking by water: block from-to event removes plant
 * - Farmland removed: physics event removes plant
 * - Harvesting: mature plant (age 7) drops reward and notifies chat
 */
public class PlantListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final PlantsPlugin plugin;
    private final PlantDataManager dataManager;
    private final DropManager dropManager;

    public PlantListener(PlantsPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getPlantDataManager();
        this.dropManager = new DropManager(plugin);
    }

    /**
     * Handle planting the custom seed on right-click.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlantSeed(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (!SeedItem.isLungiSeed(item)) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        // Must click on farmland
        if (clickedBlock.getType() != Material.FARMLAND) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), plugin.getConfig().getString("messages.no-farmland",
                    "<red>Рослину можна садити лише на пашню!"));
            return;
        }

        // The block above must be air
        Block plantBlock = clickedBlock.getRelative(0, 1, 0);
        if (plantBlock.getType() != Material.AIR) return;

        event.setCancelled(true);

        // Place wheat at stage 0
        plantBlock.setType(Material.WHEAT);
        setWheatAge(plantBlock, GrowthStage.STAGE_1.getWheatAge());

        // Register as custom plant
        dataManager.addPlant(plantBlock.getLocation());

        // Consume one seed from player's hand (not in creative)
        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
        }
    }

    /**
     * Handle plant growth - control our 5-stage growth cycle.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlantGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        if (!dataManager.isCustomPlant(block.getLocation())) return;
        if (block.getType() != Material.WHEAT) return;

        event.setCancelled(true);

        // Advance one stage in our 5-stage system
        int currentAge = ((Ageable) block.getBlockData()).getAge();
        GrowthStage currentStage = GrowthStage.fromWheatAge(currentAge);

        if (currentStage.isMature()) return; // Already mature

        GrowthStage nextStage = currentStage.next();
        setWheatAge(block, nextStage.getWheatAge());
    }

    /**
     * Handle breaking the custom plant.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlantBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!dataManager.isCustomPlant(block.getLocation())) return;

        // Cancel default drops
        event.setDropItems(false);
        dataManager.removePlant(block.getLocation());

        // Only give reward if fully grown
        if (block.getType() == Material.WHEAT) {
            int age = ((Ageable) block.getBlockData()).getAge();
            GrowthStage stage = GrowthStage.fromWheatAge(age);
            if (stage.isMature()) {
                dropManager.handleHarvest(event.getPlayer());
            }
        }
    }

    /**
     * Handle water flow destroying the custom plant.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWaterFlow(BlockFromToEvent event) {
        Block toBlock = event.getToBlock();
        if (!dataManager.isCustomPlant(toBlock.getLocation())) return;

        // Water breaks the plant - remove tracking and let the water flow naturally
        dataManager.removePlant(toBlock.getLocation());
        toBlock.setType(Material.AIR);
        // No rewards for water-broken plants
    }

    /**
     * Handle farmland being destroyed (physics removes crop on top).
     * When farmland is broken/trampled, the crop above will be destroyed by physics.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.WHEAT) return;
        if (!dataManager.isCustomPlant(block.getLocation())) return;

        // Check if the block below is no longer farmland
        Block below = block.getRelative(0, -1, 0);
        if (below.getType() != Material.FARMLAND) {
            dataManager.removePlant(block.getLocation());
            block.setType(Material.AIR);
        }
    }

    /**
     * Handle farmland breaking - remove plant on top if it's our custom plant.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFarmlandBreak(BlockBreakEvent event) {
        Block farmland = event.getBlock();
        if (farmland.getType() != Material.FARMLAND) return;

        Block above = farmland.getRelative(0, 1, 0);
        if (above.getType() == Material.WHEAT && dataManager.isCustomPlant(above.getLocation())) {
            dataManager.removePlant(above.getLocation());
            above.setType(Material.AIR);
        }
    }

    /**
     * Prevent the plant from being trampled or changed by other means.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        // When farmland dries/is removed and crop on top is custom
        if (block.getType() == Material.FARMLAND) {
            Block above = block.getRelative(0, 1, 0);
            if (above.getType() == Material.WHEAT && dataManager.isCustomPlant(above.getLocation())) {
                // Schedule removal on next tick after farmland fades
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (above.getType() == Material.WHEAT || above.getType() == Material.AIR) {
                        if (dataManager.isCustomPlant(above.getLocation())) {
                            dataManager.removePlant(above.getLocation());
                            above.setType(Material.AIR);
                        }
                    }
                });
            }
        }
    }

    private void setWheatAge(Block block, int age) {
        BlockData data = block.getBlockData();
        if (data instanceof Ageable ageable) {
            ageable.setAge(Math.min(age, ageable.getMaximumAge()));
            block.setBlockData(ageable);
        }
    }

    private void sendMsg(org.bukkit.entity.Player player, String msg) {
        player.sendMessage(MM.deserialize(toLegacyMM(msg)));
    }

    private String toLegacyMM(String s) {
        return s.replace("&0", "<black>").replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>").replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                .replace("&6", "<gold>").replace("&7", "<gray>")
                .replace("&8", "<dark_gray>").replace("&9", "<blue>")
                .replace("&a", "<green>").replace("&b", "<aqua>")
                .replace("&c", "<red>").replace("&d", "<light_purple>")
                .replace("&e", "<yellow>").replace("&f", "<white>")
                .replace("&l", "<bold>").replace("&r", "<reset>");
    }
}
