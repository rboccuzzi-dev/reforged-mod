package com.cuzzi.reforged.titles;

import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TitleRegistry {

    public static final Map<String, TitleDefinition> REGISTRY = new HashMap<>();
    private static final Set<String> BUILT_IN_IDS = new HashSet<>();


    public static final TitleDefinition FIRST_MYTHIC_SMITH = registerBuiltIn(new TitleDefinition(
            "first_mythic_smith",
            Component.literal("First Mythic Smith"),
            ItemRarity.MYTHIC,
            Map.of(StatType.STRENGTH, 5, StatType.INTELLIGENCE, 5),
            Map.of(StatType.STRENGTH, 10, StatType.INTELLIGENCE, 10),
            2,
            3,
            List.of(StatType.AURA)
    ));

    public static final TitleDefinition FIRST_LEGENDARY_SMITH = registerBuiltIn(new TitleDefinition(
            "first_legendary_smith",
            Component.literal("First Legendary Smith"),
            ItemRarity.LEGENDARY,
            Map.of(StatType.STRENGTH, 2, StatType.ENDURANCE, 2), // Inactive bonuses
            Map.of(StatType.STRENGTH, 5, StatType.ENDURANCE, 5), // Active bonuses
            1, // Inactive all stats bonus
            2, // Active all stats bonus
            List.of(StatType.AURA)
    ));

    public static final TitleDefinition NOVICE_FORGER = registerBuiltIn(new TitleDefinition(
            "novice_forger",
            Component.literal("Novice Forger"),
            ItemRarity.COMMON,
            Map.of(StatType.STRENGTH, 1),
            Map.of(),
            0,
            0,
            List.of()
    ));

    public static final TitleDefinition AURA_WARRIOR = registerBuiltIn(new TitleDefinition(
            "aura_warrior",
            Component.literal("Aura Warrior"),
            ItemRarity.RARE,
            Map.of(StatType.AURA, 5),
            Map.of(StatType.STRENGTH, 4, StatType.AURA, 10),
            0,
            0,
            List.of(StatType.AURA)
    ));

    private static TitleDefinition registerBuiltIn(TitleDefinition def) {
        BUILT_IN_IDS.add(def.id());
        return register(def);
    }

    public static TitleDefinition register(TitleDefinition def) {
        REGISTRY.put(def.id(), def);
        return def;
    }

    public static TitleDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static boolean isCustomTitle(String id) {
        return !BUILT_IN_IDS.contains(id);
    }
}