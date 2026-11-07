package com.cuzzi.reforged.rarity;

public final class JobRarityRules {

    public static int pointsPerLevel(Rarity rarity) {
        return switch (rarity) {
            case COMMON, UNCOMMON -> 2;
            case RARE, EPIC -> 4;
            case UNIQUE, LEGENDARY, MYTHIC -> 6;
        };
    }

    public static float levelXpMultiplier(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1.0f;
            case UNCOMMON -> 1.15f;
            case RARE -> 1.25f;
            case EPIC -> 1.40f;
            case UNIQUE -> 1.60f;
            case LEGENDARY -> 1.80f;
            case MYTHIC -> 2.00f;
        };
    }

    private JobRarityRules() {}
}