package com.lungi.roles.data;

import com.lungi.roles.RolesPlugin;
import com.lungi.roles.role.RoleType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages loading and saving player data to plugins/RolesLungi/players.yml
 */
public class PlayerDataManager {

    private final RolesPlugin plugin;
    private final File dataFile;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(RolesPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
    }

    public void load() {
        if (!dataFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        if (playersSection == null) return;

        for (String uuidStr : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerData data = new PlayerData(uuid);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidStr);
                if (playerSection == null) continue;

                for (RoleType role : RoleType.values()) {
                    String key = role.getKey();
                    data.setRoleExp(role, playerSection.getLong(key + ".exp", 0));
                    data.setRoleLevel(role, playerSection.getInt(key + ".level", 0));

                    List<?> claimed = playerSection.getList(key + ".claimed-milestones", new ArrayList<>());
                    for (Object obj : claimed) {
                        if (obj instanceof Integer lvl) {
                            data.claimMilestone(role, lvl);
                        }
                    }
                }

                data.setSkillPoints(playerSection.getInt("skillPoints", 0));
                data.setVitalityLevel(playerSection.getInt("vitality", 0));
                data.setDamageLevel(playerSection.getInt("damage", 0));
                data.setDefenseLevel(playerSection.getInt("defense", 0));
                data.setKnightCritActive(playerSection.getBoolean("knightCritActive", false));
                data.setBuilderHourStart(playerSection.getLong("builderHourStart", 0));
                data.setBuilderExpThisHour(playerSection.getInt("builderExpThisHour", 0));
                data.setFarmerSkillUnlocked(playerSection.getBoolean("farmerSkillUnlocked", false));
                data.setMinerBonusDropUnlocked(playerSection.getBoolean("minerBonusDropUnlocked", false));
                data.setMinerAutoSmeltUnlocked(playerSection.getBoolean("minerAutoSmeltUnlocked", false));

                cache.put(uuid, data);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in players.yml: " + uuidStr);
            }
        }

        plugin.getLogger().info("Loaded data for " + cache.size() + " players.");
    }

    public void saveAll() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, PlayerData> entry : cache.entrySet()) {
            String uuidStr = entry.getKey().toString();
            PlayerData data = entry.getValue();

            for (RoleType role : RoleType.values()) {
                String base = "players." + uuidStr + "." + role.getKey();
                config.set(base + ".exp", data.getRoleExp(role));
                config.set(base + ".level", data.getRoleLevel(role));
                config.set(base + ".claimed-milestones",
                        new ArrayList<>(data.getClaimedMilestones(role)));
            }

            String base = "players." + uuidStr;
            config.set(base + ".skillPoints", data.getSkillPoints());
            config.set(base + ".vitality", data.getVitalityLevel());
            config.set(base + ".damage", data.getDamageLevel());
            config.set(base + ".defense", data.getDefenseLevel());
            config.set(base + ".knightCritActive", data.isKnightCritActive());
            config.set(base + ".builderHourStart", data.getBuilderHourStart());
            config.set(base + ".builderExpThisHour", data.getBuilderExpThisHour());
            config.set(base + ".farmerSkillUnlocked", data.isFarmerSkillUnlocked());
            config.set(base + ".minerBonusDropUnlocked", data.isMinerBonusDropUnlocked());
            config.set(base + ".minerAutoSmeltUnlocked", data.isMinerAutoSmeltUnlocked());
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    public PlayerData getOrCreate(UUID uuid) {
        return cache.computeIfAbsent(uuid, PlayerData::new);
    }

    public Optional<PlayerData> get(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }

    public boolean has(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void remove(UUID uuid) {
        cache.remove(uuid);
    }
}
