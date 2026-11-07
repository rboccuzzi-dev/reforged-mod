package com.cuzzi.reforged.titles;

import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public record TitleDefinition(
        String id,
        Component displayName,
        ItemRarity rarity,
        Map<StatType, Integer> inactiveBonuses, // Bonus pasivos permanentes (al poseer)
        Map<StatType, Integer> activeBonuses,   // Bonus extra (solo al equipar como Activo)
        int inactiveAllStatsBonus,
        int activeAllStatsBonus,
        List<StatType> unlockedStats            // Stats especiales reveladas permanentemente
) {}