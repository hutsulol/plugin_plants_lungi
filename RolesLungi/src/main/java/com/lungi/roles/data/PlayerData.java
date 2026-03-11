package com.lungi.roles.data;

import com.lungi.roles.role.RoleType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds all role-related data for a single player.
 */
public class PlayerData {

    private final UUID uuid;

    // Role experience points
    private final EnumMap<RoleType, Long> roleExp = new EnumMap<>(RoleType.class);
    // Role levels (derived from exp, stored for quick access)
    private final EnumMap<RoleType, Integer> roleLevels = new EnumMap<>(RoleType.class);
    // Milestone rewards already claimed (role -> set of levels claimed)
    private final EnumMap<RoleType, java.util.Set<Integer>> claimedMilestones = new EnumMap<>(RoleType.class);

    // Skill points (shared pool from Farmer, Miner, Knight level-ups)
    private int skillPoints = 0;

    // Purchased skills
    private int vitalityLevel = 0;   // Each level = +2 max HP (1 heart)
    private int damageLevel = 0;     // Each level = +0.5 attack damage
    private int defenseLevel = 0;    // Each level = higher resistance tier (max 3)

    // Knight crit trade flag (permanently lost 1 heart for crit)
    private boolean knightCritActive = false;

    // Builder hourly tracking
    private long builderHourStart = 0;
    private int builderExpThisHour = 0;

    // Farmer skill unlocked (level 5+)
    private boolean farmerSkillUnlocked = false;

    // Miner skills
    private boolean minerBonusDropUnlocked = false;   // Level 5
    private boolean minerAutoSmeltUnlocked = false;    // Level 10

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        for (RoleType role : RoleType.values()) {
            roleExp.put(role, 0L);
            roleLevels.put(role, 0);
            claimedMilestones.put(role, new java.util.HashSet<>());
        }
    }

    // --- Getters / Setters ---

    public UUID getUuid() { return uuid; }

    public long getRoleExp(RoleType role) { return roleExp.getOrDefault(role, 0L); }
    public void setRoleExp(RoleType role, long exp) { roleExp.put(role, exp); }
    public void addRoleExp(RoleType role, long amount) {
        roleExp.put(role, getRoleExp(role) + amount);
    }

    public int getRoleLevel(RoleType role) { return roleLevels.getOrDefault(role, 0); }
    public void setRoleLevel(RoleType role, int level) { roleLevels.put(role, level); }

    public boolean hasMilestoneClaimed(RoleType role, int level) {
        return claimedMilestones.getOrDefault(role, java.util.Collections.emptySet()).contains(level);
    }
    public void claimMilestone(RoleType role, int level) {
        claimedMilestones.computeIfAbsent(role, k -> new java.util.HashSet<>()).add(level);
    }
    public java.util.Set<Integer> getClaimedMilestones(RoleType role) {
        return claimedMilestones.getOrDefault(role, java.util.Collections.emptySet());
    }

    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int skillPoints) { this.skillPoints = skillPoints; }
    public void addSkillPoints(int amount) { this.skillPoints += amount; }

    public int getVitalityLevel() { return vitalityLevel; }
    public void setVitalityLevel(int vitalityLevel) { this.vitalityLevel = vitalityLevel; }

    public int getDamageLevel() { return damageLevel; }
    public void setDamageLevel(int damageLevel) { this.damageLevel = damageLevel; }

    public int getDefenseLevel() { return defenseLevel; }
    public void setDefenseLevel(int defenseLevel) { this.defenseLevel = defenseLevel; }

    public boolean isKnightCritActive() { return knightCritActive; }
    public void setKnightCritActive(boolean knightCritActive) { this.knightCritActive = knightCritActive; }

    public long getBuilderHourStart() { return builderHourStart; }
    public void setBuilderHourStart(long builderHourStart) { this.builderHourStart = builderHourStart; }

    public int getBuilderExpThisHour() { return builderExpThisHour; }
    public void setBuilderExpThisHour(int builderExpThisHour) { this.builderExpThisHour = builderExpThisHour; }

    public boolean isFarmerSkillUnlocked() { return farmerSkillUnlocked; }
    public void setFarmerSkillUnlocked(boolean farmerSkillUnlocked) { this.farmerSkillUnlocked = farmerSkillUnlocked; }

    public boolean isMinerBonusDropUnlocked() { return minerBonusDropUnlocked; }
    public void setMinerBonusDropUnlocked(boolean minerBonusDropUnlocked) { this.minerBonusDropUnlocked = minerBonusDropUnlocked; }

    public boolean isMinerAutoSmeltUnlocked() { return minerAutoSmeltUnlocked; }
    public void setMinerAutoSmeltUnlocked(boolean minerAutoSmeltUnlocked) { this.minerAutoSmeltUnlocked = minerAutoSmeltUnlocked; }
}
