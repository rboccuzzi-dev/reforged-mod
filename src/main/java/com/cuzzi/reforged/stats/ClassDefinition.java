package com.cuzzi.reforged.stats;

import com.cuzzi.reforged.rarity.Rarity;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Defines which stats a class has and how each one behaves.
 * A stat that is not in the class simply doesn't appear on the screen.
 */
public final class ClassDefinition {

    static int MAX_REAL = 500000;
    /**
     * base: value at 0 points spent
     * max:  upper cap
     * step: how much the value changes per point spent
     */
    public record StatRule(int base, int max, int step) {}

    public static final ClassDefinition WARRIOR = new ClassDefinition("warrior", Rarity.COMMON, 10)
            .rule(StatType.STRENGTH,      5,  MAX_REAL, 1)
            .rule(StatType.AGILITY,       3,  MAX_REAL, 1)
            .rule(StatType.ENDURANCE,     4,  MAX_REAL, 1)
            .rule(StatType.INTELLIGENCE,  1,  MAX_REAL, 1)
            .rule(StatType.STAMINA,       5,  MAX_REAL, 1);

    public static final ClassDefinition MAGE = new ClassDefinition("mage", Rarity.UNCOMMON, 10)
            .rule(StatType.INTELLIGENCE,  6,  MAX_REAL, 1)
            .rule(StatType.AGILITY,       3,  MAX_REAL, 1)
            .rule(StatType.ENDURANCE,     2,  MAX_REAL, 1)
            .rule(StatType.STAMINA,       5,  MAX_REAL, 1);

    public static final List<ClassDefinition> ALL = List.of(WARRIOR, MAGE);

    public static ClassDefinition byId(String id) {
        for (ClassDefinition c : ALL) {
            if (c.id.equals(id)) return c;
        }
        return WARRIOR;
    }

    private final String id;
    private final Rarity rarity;
    private final int startingPoints;
    private final Map<StatType, StatRule> rules = new EnumMap<>(StatType.class);

    private ClassDefinition(String id, Rarity rarity, int startingPoints) {
        this.id = id;
        this.rarity = rarity;
        this.startingPoints = startingPoints;
    }

    private ClassDefinition rule(StatType type, int base, int max, int step) {
        rules.put(type, new StatRule(base, max, step));
        return this;
    }

    public String id() {
        return id;
    }

    public Rarity rarity() {
        return rarity;
    }

    public int startingPoints() {
        return startingPoints;
    }

    public Map<StatType, StatRule> rules() {
        return rules;
    }

    public StatRule rule(StatType type) {
        return rules.get(type);
    }
}