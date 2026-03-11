package com.lungi.roles;

import com.lungi.roles.api.RolesAPI;
import com.lungi.roles.antiabuse.AntiAbuseManager;
import com.lungi.roles.command.RolesAdminCommand;
import com.lungi.roles.command.RolesCommand;
import com.lungi.roles.data.PlayerDataManager;
import com.lungi.roles.gui.GUIListener;
import com.lungi.roles.level.LevelManager;
import com.lungi.roles.listener.*;
import com.lungi.roles.skill.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RolesPlugin extends JavaPlugin {

    private static RolesPlugin instance;

    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;
    private SkillManager skillManager;
    private AntiAbuseManager antiAbuseManager;
    private RolesAPI rolesAPI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        playerDataManager = new PlayerDataManager(this);
        playerDataManager.load();

        levelManager = new LevelManager(this);
        skillManager = new SkillManager(this);
        antiAbuseManager = new AntiAbuseManager(this);
        rolesAPI = new RolesAPI(this);

        // Register listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new FarmerListener(this), this);
        pm.registerEvents(new LumberjackListener(this), this);
        pm.registerEvents(new MinerListener(this), this);
        pm.registerEvents(new KnightListener(this), this);
        pm.registerEvents(new FisherListener(this), this);
        pm.registerEvents(new BuilderListener(this), this);
        pm.registerEvents(new PlayerSessionListener(this), this);
        pm.registerEvents(new GUIListener(this), this);

        // Register commands
        var rolesCmd = getCommand("roles");
        if (rolesCmd != null) rolesCmd.setExecutor(new RolesCommand(this));

        var adminCmd = getCommand("roleadmin");
        if (adminCmd != null) adminCmd.setExecutor(new RolesAdminCommand(this));

        // Apply skill effects to all online players
        getServer().getOnlinePlayers().forEach(skillManager::applyEffects);

        getLogger().info("RolesLungi enabled!");
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("RolesLungi disabled!");
    }

    public static RolesPlugin getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public AntiAbuseManager getAntiAbuseManager() {
        return antiAbuseManager;
    }

    public RolesAPI getRolesAPI() {
        return rolesAPI;
    }
}
