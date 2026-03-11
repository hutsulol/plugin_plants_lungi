package com.lungi.roles.role;

public enum RoleType {
    FARMER("Фермер", "farmer", "🌾"),
    LUMBERJACK("Лісоруб", "lumberjack", "🪓"),
    MINER("Шахтар", "miner", "⛏"),
    KNIGHT("Рицар", "knight", "⚔"),
    FISHER("Рибак", "fisher", "🎣"),
    BUILDER("Будівник", "builder", "🏗");

    private final String displayName;
    private final String key;
    private final String icon;

    RoleType(String displayName, String key, String icon) {
        this.displayName = displayName;
        this.key = key;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
        return key;
    }

    public String getIcon() {
        return icon;
    }

    public static RoleType fromKey(String key) {
        for (RoleType role : values()) {
            if (role.key.equalsIgnoreCase(key)) return role;
        }
        return null;
    }
}
