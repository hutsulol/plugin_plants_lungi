package com.lungi.roles.skill;

public enum SkillType {
    VITALITY("Живучість", "Додаткові серця здоров'я (+1 серце за рівень)", 5),
    DAMAGE("Урон", "Збільшений урон (+0.5 за рівень)", 5),
    DEFENSE("Захист", "Постійний ефект опору (макс. рівень 3)", 3);

    private final String displayName;
    private final String description;
    private final int maxLevel;

    SkillType(String displayName, String description, int maxLevel) {
        this.displayName = displayName;
        this.description = description;
        this.maxLevel = maxLevel;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getMaxLevel() { return maxLevel; }
}
