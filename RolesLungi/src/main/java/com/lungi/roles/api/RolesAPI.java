package com.lungi.roles.api;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.entity.Player;

/**
 * Public API for other plugins to interact with RolesLungi.
 * Used by PlantsLungi to get farmer level and add farmer exp.
 */
public class RolesAPI {

    private static RolesAPI instance;
    private final RolesPlugin plugin;

    public RolesAPI(RolesPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static RolesAPI getInstance() {
        return instance;
    }

    /**
     * Get the farmer level of a player.
     */
    public int getFarmerLevel(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        return data.getRoleLevel(RoleType.FARMER);
    }

    /**
     * Add experience to a player's role.
     * @param player the player
     * @param roleKey the role key (e.g., "FARMER", "MINER", etc.)
     * @param amount experience amount to add
     */
    public void addRoleExp(Player player, String roleKey, int amount) {
        RoleType role = RoleType.fromKey(roleKey);
        if (role == null) {
            // Try by enum name
            try {
                role = RoleType.valueOf(roleKey.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unknown role: " + roleKey);
                return;
            }
        }
        plugin.getLevelManager().addExp(player, role, amount);
    }

    /**
     * Get the level of any role for a player.
     */
    public int getRoleLevel(Player player, String roleKey) {
        RoleType role = RoleType.fromKey(roleKey);
        if (role == null) return 0;
        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        return data.getRoleLevel(role);
    }

    /**
     * Get the total experience of a player in a role.
     */
    public long getRoleExp(Player player, String roleKey) {
        RoleType role = RoleType.fromKey(roleKey);
        if (role == null) return 0;
        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        return data.getRoleExp(role);
    }
}
