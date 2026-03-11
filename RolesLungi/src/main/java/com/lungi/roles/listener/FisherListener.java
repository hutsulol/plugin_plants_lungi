package com.lungi.roles.listener;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.role.RoleType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Tracks fisher actions: catching anything while fishing gives exp.
 */
public class FisherListener implements Listener {

    private final RolesPlugin plugin;

    public FisherListener(RolesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH &&
            event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;

        Player player = event.getPlayer();

        // Only award if something was caught (not just a bite without reel)
        if (event.getCaught() == null && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) return;

        int expGain = plugin.getConfig().getInt("exp-values.fisher-catch", 2);
        plugin.getLevelManager().addExp(player, RoleType.FISHER, expGain);
    }
}
