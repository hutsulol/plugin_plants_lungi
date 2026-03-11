package com.lungi.roles.antiabuse;

import com.lungi.roles.RolesPlugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * Prevents abuse of knight player-kill exp farming.
 * - Per-victim cooldown (configurable minutes)
 * - Max kills per hour limit
 */
public class AntiAbuseManager {

    private final RolesPlugin plugin;
    // killerUUID -> victimUUID -> last kill timestamp
    private final Map<UUID, Map<UUID, Long>> killCooldowns = new HashMap<>();
    // killerUUID -> timestamps of kills this hour
    private final Map<UUID, Queue<Long>> hourlyKills = new HashMap<>();

    public AntiAbuseManager(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a knight player kill should award exp.
     * Applies victim cooldown and hourly limit.
     */
    public boolean shouldAwardKillExp(UUID killerUUID, UUID victimUUID) {
        long now = System.currentTimeMillis();
        long cooldownMs = plugin.getConfig().getLong("knight-kill-cooldown-minutes", 60) * 60_000L;
        int maxPerHour = plugin.getConfig().getInt("knight-max-kills-per-hour", 5);

        // Check victim cooldown
        Map<UUID, Long> victimMap = killCooldowns.computeIfAbsent(killerUUID, k -> new HashMap<>());
        Long lastKill = victimMap.get(victimUUID);
        if (lastKill != null && (now - lastKill) < cooldownMs) {
            return false;
        }

        // Check hourly limit
        Queue<Long> hourKills = hourlyKills.computeIfAbsent(killerUUID, k -> new LinkedList<>());
        // Remove kills older than 1 hour
        while (!hourKills.isEmpty() && (now - hourKills.peek()) > 3_600_000L) {
            hourKills.poll();
        }
        if (hourKills.size() >= maxPerHour) {
            return false;
        }

        // Record kill
        victimMap.put(victimUUID, now);
        hourKills.add(now);
        return true;
    }

    /**
     * Reset tracking data when the plugin restarts.
     */
    public void clear() {
        killCooldowns.clear();
        hourlyKills.clear();
    }
}
