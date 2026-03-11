package com.lungi.plants;

import com.lungi.plants.api.RolesAPIBridge;
import com.lungi.plants.command.SeedCommand;
import com.lungi.plants.listener.PlantListener;
import com.lungi.plants.listener.ResourcePackListener;
import com.lungi.plants.manager.PlantDataManager;
import com.lungi.plants.plant.GrowthStage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class PlantsPlugin extends JavaPlugin {

    private static PlantsPlugin instance;
    private PlantDataManager plantDataManager;
    private RolesAPIBridge rolesAPIBridge;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        plantDataManager = new PlantDataManager(this);
        plantDataManager.load();

        rolesAPIBridge = new RolesAPIBridge(this);

        var pm = getServer().getPluginManager();
        pm.registerEvents(new PlantListener(this), this);
        pm.registerEvents(new ResourcePackListener(this), this);

        var seedCmd = getCommand("plantseed");
        if (seedCmd != null) {
            seedCmd.setExecutor(new SeedCommand(this));
        }

        // Growth timer — advances all custom plants one stage every growth-ticks ticks
        int growthTicks = getConfig().getInt("growth-ticks", 200);
        getServer().getScheduler().runTaskTimer(this, this::tickGrowth, growthTicks, growthTicks);

        getLogger().info("PlantsLungi enabled! Growth interval: " + growthTicks + " ticks.");
    }

    /** Called every growth-ticks ticks to advance all custom plants one growth stage. */
    private void tickGrowth() {
        for (Location loc : new ArrayList<>(plantDataManager.getLocations())) {
            Block block = loc.getBlock();
            if (block.getType() != Material.WHEAT) {
                plantDataManager.removePlant(loc);
                continue;
            }
            BlockData bd = block.getBlockData();
            if (!(bd instanceof Ageable ageable)) continue;
            GrowthStage stage = GrowthStage.fromWheatAge(ageable.getAge());
            if (stage.isMature()) continue;
            GrowthStage next = stage.next();
            ageable.setAge(Math.min(next.getWheatAge(), ageable.getMaximumAge()));
            block.setBlockData(ageable);
        }
    }

    @Override
    public void onDisable() {
        if (plantDataManager != null) {
            plantDataManager.save();
        }
        getLogger().info("PlantsLungi disabled!");
    }

    public static PlantsPlugin getInstance() {
        return instance;
    }

    public PlantDataManager getPlantDataManager() {
        return plantDataManager;
    }

    public RolesAPIBridge getRolesAPIBridge() {
        return rolesAPIBridge;
    }
}
