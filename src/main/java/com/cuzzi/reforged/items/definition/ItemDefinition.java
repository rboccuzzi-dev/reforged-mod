package com.cuzzi.reforged.items.definition;

import com.cuzzi.reforged.stats.StatType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.HashMap;
import java.util.Map;

public record ItemDefinition(
        String id,
        Item baseMinecraftItem,
        Map<String, Double> baseVanillaAttributes, // These are now percentages of the Power Budget
        Map<StatType, Double> baseCustomStats
) {
    public static final Map<String, ItemDefinition> REGISTRY = new HashMap<>();

    static {
        // Sword distributes 40% to damage, 25% to speed, 35% to Reforged Stats
        Map<String, Double> swordVanilla = new HashMap<>();
        swordVanilla.put("attack_damage", 0.40);
        swordVanilla.put("attack_speed", 0.25);

        Map<StatType, Double> swordCustom = new HashMap<>();
        swordCustom.put(StatType.STRENGTH, 0.20);
        swordCustom.put(StatType.AGILITY, 0.15);

        REGISTRY.put("iron_sword", new ItemDefinition("iron_sword", Items.IRON_SWORD, swordVanilla, swordCustom));
        REGISTRY.put("diamond_sword", new ItemDefinition("diamond_sword", Items.DIAMOND_SWORD, swordVanilla, swordCustom));
    }
}