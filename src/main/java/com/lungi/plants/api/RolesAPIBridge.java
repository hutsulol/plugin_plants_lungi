package com.lungi.plants.api;

import com.lungi.plants.PlantsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Bridge to the RolesLungi plugin API via reflection.
 * Allows Plants plugin to work without a hard dependency on Roles plugin.
 */
public class RolesAPIBridge {

    private final PlantsPlugin plugin;
    private Object rolesAPI = null;
    private Method getFarmerLevelMethod = null;
    private Method addFarmerExpMethod = null;
    private boolean initialized = false;

    public RolesAPIBridge(PlantsPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        if (initialized) return;
        initialized = true;

        Plugin rolesPlugin = plugin.getServer().getPluginManager().getPlugin("RolesLungi");
        if (rolesPlugin == null) {
            plugin.getLogger().warning("RolesLungi not found - farmer level bonuses disabled.");
            return;
        }

        try {
            Class<?> apiClass = rolesPlugin.getClass().getClassLoader()
                    .loadClass("com.lungi.roles.api.RolesAPI");
            getFarmerLevelMethod = apiClass.getMethod("getFarmerLevel", Player.class);
            addFarmerExpMethod = apiClass.getMethod("addRoleExp", Player.class, String.class, int.class);
            // Get the singleton instance
            Method getInstance = apiClass.getMethod("getInstance");
            rolesAPI = getInstance.invoke(null);
            plugin.getLogger().info("RolesLungi API connected successfully.");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect to RolesLungi API: " + e.getMessage());
        }
    }

    /**
     * Returns the farmer level of a player (0 if Roles plugin is not available).
     */
    public int getFarmerLevel(Player player) {
        if (rolesAPI == null || getFarmerLevelMethod == null) return 0;
        try {
            Object result = getFarmerLevelMethod.invoke(rolesAPI, player);
            return (int) result;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Adds farmer experience to the player.
     */
    public void addFarmerExp(Player player, int amount) {
        if (rolesAPI == null || addFarmerExpMethod == null) return;
        try {
            addFarmerExpMethod.invoke(rolesAPI, player, "FARMER", amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add farmer exp: " + e.getMessage());
        }
    }

    public boolean isAvailable() {
        return rolesAPI != null;
    }
}
