package com.lungi.plants.plant;

/**
 * Represents growth stages of the custom Lungi plant.
 * Maps to wheat age values: 0,2,4,6,7 for 5 visible stages.
 */
public enum GrowthStage {
    STAGE_1(0),   // Seedling
    STAGE_2(2),   // Sprout
    STAGE_3(4),   // Growing
    STAGE_4(6),   // Almost ripe
    STAGE_5(7);   // Mature / harvestable

    private final int wheatAge;

    GrowthStage(int wheatAge) {
        this.wheatAge = wheatAge;
    }

    public int getWheatAge() {
        return wheatAge;
    }

    public boolean isMature() {
        return this == STAGE_5;
    }

    public GrowthStage next() {
        return switch (this) {
            case STAGE_1 -> STAGE_2;
            case STAGE_2 -> STAGE_3;
            case STAGE_3 -> STAGE_4;
            case STAGE_4 -> STAGE_5;
            case STAGE_5 -> STAGE_5;
        };
    }

    /**
     * Get the growth stage corresponding to a wheat age value.
     */
    public static GrowthStage fromWheatAge(int age) {
        if (age <= 1) return STAGE_1;
        if (age <= 3) return STAGE_2;
        if (age <= 5) return STAGE_3;
        if (age == 6) return STAGE_4;
        return STAGE_5;
    }
}
