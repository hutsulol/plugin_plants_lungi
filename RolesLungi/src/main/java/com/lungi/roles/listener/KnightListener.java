package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.antiabuse.AntiAbuseManager;
import com.lungi.roles.data.PlayerData;
import com.lungi.roles.role.RoleType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

/**
 * Tracks knight actions: killing mobs and players.
 * Knight crit: 5% chance for 200% damage (trades 1 heart permanently).
 * Anti-abuse: prevents farming player-kill exp.
 */
public class KnightListener implements Listener {

    private final RolesPlugin plugin;
    private final Random random = new Random();

    public KnightListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply critical hit before damage is calculated.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        PlayerData data = plugin.getPlayerDataManager().getOrCreate(player.getUniqueId());
        if (!data.isKnightCritActive()) return;

        double critChance = plugin.getConfig().getDouble("knight-crit-chance", 5.0);
        double critMultiplier = plugin.getConfig().getDouble("knight-crit-multiplier", 2.0);

        if (random.nextDouble() * 100 < critChance) {
            event.setDamage(event.getDamage() * critMultiplier);
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "§c★ КРИТ! ★"
            ));
        }
    }

    /**
     * Award exp when a kill happens.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        if (entity instanceof Player victim) {
            // Player kill - check anti-abuse
            AntiAbuseManager antiAbuse = plugin.getAntiAbuseManager();
            if (!antiAbuse.shouldAwardKillExp(killer.getUniqueId(), victim.getUniqueId())) {
                killer.sendMessage("§7[Рицар] Вбивство не зараховано (захист від абузу).");
                return;
            }
            int exp = plugin.getConfig().getInt("exp-values.knight-player-kill", 5);
            plugin.getLevelManager().addExp(killer, RoleType.KNIGHT, exp);

        } else if (entity instanceof Monster) {
            // Hostile mob
            int exp = plugin.getConfig().getInt("exp-values.knight-hostile-mob", 3);
            plugin.getLevelManager().addExp(killer, RoleType.KNIGHT, exp);

        } else {
            // Peaceful/neutral mob
            int exp = plugin.getConfig().getInt("exp-values.knight-neutral-mob", 1);
            plugin.getLevelManager().addExp(killer, RoleType.KNIGHT, exp);
        }
    }
}
