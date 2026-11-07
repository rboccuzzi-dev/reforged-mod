package com.cuzzi.reforged.items.modifier;

import com.cuzzi.reforged.stats.StatType;
import java.util.HashMap;
import java.util.Map;

/** Modifiers alter both vanilla attributes and custom Reforged stats. */
public enum CraftingModifier {
    NONE("none"),
    FAST("fast"),
    HEAVY("heavy"),
    SHARP("sharp"),
    REINFORCED("reinforced"),
    BALANCED("balanced");

    private final String id;

    CraftingModifier(String id) {
        this.id = id;
    }

    public static CraftingModifier byId(String id) {
        for (CraftingModifier m : values()) {
            if (m.id.equalsIgnoreCase(id)) return m;
        }
        return NONE;
    }

    // Applies modifier logic based on the design rules.
    // In the future, this can be moved to a JSON-driven Datapack registry.
    public void applyToStats(Map<StatType, Double> customStats, Map<String, Double> vanillaStats, float quality) {
        // Quality dictates how well the modifier was executed (0.0 to 1.0)
        double efficiency = 0.5 + (0.5 * quality);

        switch (this) {
            case FAST -> {
                vanillaStats.compute("attack_speed", (k, v) -> v * (1.0 + (0.3 * efficiency)));
                vanillaStats.compute("attack_damage", (k, v) -> v * (1.0 - (0.15 * efficiency)));
                customStats.computeIfPresent(StatType.AGILITY, (k, v) -> v + (5 * efficiency));
                customStats.computeIfPresent(StatType.STRENGTH, (k, v) -> v - (2 * efficiency));
            }
            case HEAVY -> {
                vanillaStats.compute("attack_damage", (k, v) -> v * (1.0 + (0.3 * efficiency)));
                vanillaStats.compute("attack_speed", (k, v) -> v * (1.0 - (0.2 * efficiency)));
                customStats.computeIfPresent(StatType.STRENGTH, (k, v) -> v + (5 * efficiency));
                customStats.computeIfPresent(StatType.AGILITY, (k, v) -> Math.max(0, v - (3 * efficiency)));
            }
            case SHARP -> {
                vanillaStats.compute("attack_damage", (k, v) -> v * (1.0 + (0.2 * efficiency)));
                // Armor penetration would be a custom stat
            }
            case REINFORCED -> {
                customStats.computeIfPresent(StatType.STRENGTH, (k, v) -> v + (3 * efficiency));
                vanillaStats.compute("attack_speed", (k, v) -> v * (1.0 - (0.1 * efficiency)));
            }
            case BALANCED -> {
                vanillaStats.replaceAll((k, v) -> v * (1.0 + (0.05 * efficiency)));
                customStats.replaceAll((k, v) -> v * (1.0 + (0.05 * efficiency)));
            }
        }
    }
}