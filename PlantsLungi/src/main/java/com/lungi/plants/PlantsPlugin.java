package com.lungi.plants;

import com.lungi.plants.api.RolesAPIBridge;
import com.lungi.plants.command.SeedCommand;
import com.lungi.plants.listener.PlantListener;
import com.lungi.plants.listener.ResourcePackListener;
import com.lungi.plants.manager.PlantDataManager;
import org.bukkit.plugin.java.JavaPlugin;

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

        getLogger().info("PlantsLungi enabled!");
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
