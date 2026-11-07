package com.cuzzi.reforged.items.generation;

import com.cuzzi.reforged.items.components.ReforgedItemData;
import com.cuzzi.reforged.items.definition.ItemDefinition;
import com.cuzzi.reforged.items.modifier.CraftingModifier;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.registry.ModDataComponents;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cuzzi.reforged.items.pipeline.ItemPipeline.getString;

public class ItemGeneratorMod {

    public static ItemStack generate(ItemDefinition def, ItemRarity rarity, CraftingModifier modifier,
                                     float quality, String creator, boolean adminBypass, String customName) {

        // 1. Copy Base Stats
        Map<String, Double> vanillaStats = new HashMap<>(def.baseVanillaAttributes());
        Map<StatType, Double> customStats = new HashMap<>(def.baseCustomStats());

        // 2. Rarity Scaling
        float rMult = rarity.multiplier();
        vanillaStats.replaceAll((k, v) -> v * rMult);
        customStats.replaceAll((k, v) -> v * rMult);

        // 3. Crafting Modifier Application
        modifier.applyToStats(customStats, vanillaStats, quality);

        // 4. Final Quality Polish
        double finalQualityMultiplier = 0.9 + (0.2 * quality);
        vanillaStats.replaceAll((k, v) -> v * finalQualityMultiplier);
        customStats.replaceAll((k, v) -> v * finalQualityMultiplier);

        // 5. Construct Item and Data Component
        ItemStack stack = new ItemStack(def.baseMinecraftItem());

        Map<String, Double> stringCustomStats = new HashMap<>();
        customStats.forEach((k, v) -> stringCustomStats.put(k.id(), v));

        ReforgedItemData itemData = new ReforgedItemData(
                1,                            // 1. version (for future data migrations)
                UUID.randomUUID(),            // 2. uniqueId
                def.id(),                     // 3. definitionId
                new HashMap<>(),              // 4. materials (empty for now until Phase 3)
                1,                            // 5. itemLevel (default to 1 for now)
                1,                            // 6. requiredLevel (default to 1 for now)
                rarity,                       // 7. rarity
                modifier,                     // 8. modifier
                quality,                      // 9. quality
                creator,                      // 10. creatorName
                adminBypass,                  // 11. isAdminCreation
                vanillaStats,                 // 12. finalVanillaStats
                stringCustomStats             // 13. finalCustomStats
        );

        stack.set(ModDataComponents.REFORGED_ITEM_DATA, itemData);

        // Apply Vanilla Attribute Modifiers
        applyVanillaAttributes(stack, vanillaStats);

        // 6. Visuals (Custom Name and Lore Formatting)
        String displayNameStr = "";

        if (customName != null && !customName.isBlank()) {
            displayNameStr = customName; // Legendary custom name takes total priority
        } else {
            if (rarity != ItemRarity.LEGENDARY && modifier != CraftingModifier.NONE) {
                displayNameStr += capitalize(modifier.name()) + " ";
            }
            displayNameStr += capitalize(def.id().replace("_", " "));
        }

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayNameStr).withStyle(rarity.color()));

        // --- LORE (Tooltip) ---
        List<Component> loreLines = new ArrayList<>();

        // Rarity Line
        loreLines.add(Component.literal("Rarity: " + capitalize(rarity.name())).withStyle(rarity.color()));

        // Modifier Line (Only for Legendary)
        if (rarity == ItemRarity.LEGENDARY && modifier != CraftingModifier.NONE) {
            loreLines.add(Component.literal("Modifier: " + capitalize(modifier.name())).withStyle(ChatFormatting.GRAY));
        }

        loreLines.add(Component.empty());

        // Stats Section
        loreLines.add(Component.literal("Stats:").withStyle(ChatFormatting.DARK_GRAY));

        vanillaStats.forEach((key, value) -> {
            String statName = capitalize(key.replace("_", " "));
            loreLines.add(Component.literal("- " + statName + ": " + String.format("%.1f", value))
                    .withStyle(ChatFormatting.DARK_GREEN));
        });

        customStats.forEach((key, value) -> {
            String statName = key.displayName().getString();
            loreLines.add(Component.literal("- " + statName + ": " + String.format("%.1f", value))
                    .withStyle(ChatFormatting.BLUE));
        });

        stack.set(DataComponents.LORE, new ItemLore(loreLines));

        return stack;
    }

    private static void applyVanillaAttributes(ItemStack stack, Map<String, Double> stats) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        if (stats.containsKey("attack_damage")) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                            ResourceLocation.withDefaultNamespace("base_attack_damage"),
                            stats.get("attack_damage"),
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (stats.containsKey("attack_speed")) {
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                            ResourceLocation.withDefaultNamespace("base_attack_speed"),
                            stats.get("attack_speed") - 4.0, // Minecraft offsets attack speed by 4
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    private static String capitalize(String text) {
        return getString(text);
    }
}