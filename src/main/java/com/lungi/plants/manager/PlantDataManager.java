package com.lungi.plants.manager;

import com.lungi.plants.PlantsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages persistent storage of custom plant block locations.
 */
public class PlantDataManager {

    private final PlantsPlugin plugin;
    private final File dataFile;
    private final Set<Location> plantLocations = new HashSet<>();

    public PlantDataManager(PlantsPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "plants.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        var list = config.getStringList("locations");

        for (String entry : list) {
            Location loc = deserialize(entry);
            if (loc != null) {
                plantLocations.add(loc);
            }
        }
        plugin.getLogger().info("Loaded " + plantLocations.size() + " custom plant locations.");
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        var list = plantLocations.stream()
                .map(this::serialize)
                .toList();
        config.set("locations", list);

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save plant data: " + e.getMessage());
        }
    }

    public void addPlant(Location loc) {
        plantLocations.add(normalize(loc));
    }

    public void removePlant(Location loc) {
        plantLocations.remove(normalize(loc));
    }

    public boolean isCustomPlant(Location loc) {
        return plantLocations.contains(normalize(loc));
    }

    private Location normalize(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private String serialize(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location deserialize(String s) {
        String[] parts = s.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
