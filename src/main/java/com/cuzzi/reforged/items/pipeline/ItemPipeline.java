package com.cuzzi.reforged.items.pipeline;

import com.cuzzi.reforged.items.components.ReforgedItemData;
import com.cuzzi.reforged.items.definition.ItemDefinition;
import com.cuzzi.reforged.items.material.MaterialDefinition;
import com.cuzzi.reforged.items.modifier.CraftingModifier;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.registry.ModDataComponents;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemPipeline {

    public static ItemStack generate(RegistryAccess registryAccess, ItemDefinition def, Map<String, String> materials,
                                     int baseItemLevel, ItemRarity rarity, CraftingModifier modifier, float quality,
                                     String creator, boolean adminBypass, String customName) {

        int finalItemLevel = baseItemLevel;
        double damageMult = 1.0;

        // 1. Evaluate Materials & Clamp Level
        String mainMaterialId = materials.get("MAIN");
        if (mainMaterialId != null && registryAccess != null) {
            var materialRegistry = registryAccess.registryOrThrow(MaterialDefinition.REGISTRY_KEY);
            MaterialDefinition mainMat = materialRegistry.getOptional(ResourceLocation.parse(mainMaterialId)).orElse(null);

            if (mainMat != null) {
                damageMult = mainMat.damageMultiplier();
                if (finalItemLevel > mainMat.maxPotentialLevel()) {
                    finalItemLevel = mainMat.maxPotentialLevel();
                }
            }
        }

        // 2. Power Budget Allocation
        double powerBudget = 10.0 + (finalItemLevel * 2.5);

        Map<String, Double> vanillaStats = new HashMap<>();
        Map<StatType, Double> customStats = new HashMap<>();

        def.baseVanillaAttributes().forEach((key, weight) -> vanillaStats.put(key, powerBudget * weight));
        def.baseCustomStats().forEach((key, weight) -> customStats.put(key, powerBudget * weight));

        // Apply Material Multiplier
        double finalDamageMult = damageMult;
        vanillaStats.computeIfPresent("attack_damage", (k, v) -> v * finalDamageMult);

        // 3. Modifier Application
        modifier.applyToStats(customStats, vanillaStats, quality);

        // 4 & 5. Combined Quality + Rarity Scaling Pass
        double qualityMultiplier = 0.8 + (0.4 * quality);
        double netMultiplier = qualityMultiplier * rarity.multiplier();

        scaleStatsMap(vanillaStats, netMultiplier);
        scaleStatsMap(customStats, netMultiplier);

        // 6. Required Level Calculation
        int requiredLevel = calculateRequiredLevel(finalItemLevel, rarity, quality);

        // 7. Construct Item & Component Data
        ItemStack stack = new ItemStack(def.baseMinecraftItem());
        Map<String, Double> stringCustomStats = new HashMap<>();
        customStats.forEach((k, v) -> stringCustomStats.put(k.id(), v));

        ReforgedItemData itemData = new ReforgedItemData(
                1, UUID.randomUUID(), def.id(), materials, finalItemLevel, requiredLevel,
                rarity, modifier, quality, creator, adminBypass, vanillaStats, stringCustomStats
        );

        stack.set(ModDataComponents.REFORGED_ITEM_DATA, itemData);

        // 8. Apply Attributes & Visuals
        applyVanillaAttributes(stack, vanillaStats);
        buildVisuals(stack, itemData, customName);

        return stack;
    }

    private static <K> void scaleStatsMap(Map<K, Double> map, double multiplier) {
        map.replaceAll((key, value) -> value * multiplier);
    }

    private static int calculateRequiredLevel(int baseLevel, ItemRarity rarity, float quality) {
        int req = baseLevel + switch (rarity) {
            case UNCOMMON -> 1;
            case EPIC -> 3;
            case UNIQUE -> 5;
            case LEGENDARY -> 8;
            default -> 0;
        };
        if (quality >= 0.98f) req = (int) (req * 0.9);
        return Math.max(1, req);
    }

    private static void applyVanillaAttributes(ItemStack stack, Map<String, Double> stats) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        if (stats.containsKey("attack_damage")) {
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                            ResourceLocation.withDefaultNamespace("base_attack_damage"),
                            stats.get("attack_damage"), AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        if (stats.containsKey("attack_speed")) {
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
                            ResourceLocation.withDefaultNamespace("base_attack_speed"),
                            Math.min(4.0, stats.get("attack_speed")) - 4.0, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }

        // Hides standard Minecraft attribute text block at the bottom of tooltips
        ItemAttributeModifiers modifiers = builder.build().withTooltip(false);
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }

    private static void buildVisuals(ItemStack stack, ReforgedItemData data, String customName) {
        String displayNameStr = (customName != null && !customName.isBlank()) ? customName :
                capitalize(data.modifier().name()) + " " + capitalize(data.definitionId().replace("_", " "));

        stack.set(DataComponents.CUSTOM_NAME, Component.literal(displayNameStr.trim()).withStyle(data.rarity().color()));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("Lv. " + data.itemLevel() + " | Req. Lv: " + data.requiredLevel()).withStyle(ChatFormatting.YELLOW));
        lore.add(Component.literal("Rarity: " + capitalize(data.rarity().name())).withStyle(data.rarity().color()));

        if (data.rarity() == ItemRarity.LEGENDARY && data.modifier() != CraftingModifier.NONE) {
            lore.add(Component.literal("Modifier: " + capitalize(data.modifier().name())).withStyle(ChatFormatting.GRAY));
        }

        lore.add(Component.empty());
        lore.add(Component.literal("Stats:").withStyle(ChatFormatting.DARK_GRAY));

        data.finalVanillaStats().forEach((key, value) ->
                lore.add(Component.literal("- " + capitalize(key.replace("_", " ")) + ": " + String.format("%.1f", value)).withStyle(ChatFormatting.DARK_GREEN))
        );
        data.finalCustomStats().forEach((key, value) ->
                lore.add(Component.literal("- " + capitalize(key.replace("_", " ")) + ": " + String.format("%.1f", value)).withStyle(ChatFormatting.BLUE))
        );

        lore.add(Component.empty());
        lore.add(Component.literal("Forged by: " + data.creatorName()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));

        stack.set(DataComponents.LORE, new ItemLore(lore));
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty() || text.equals("NONE")) return "";
        return getString(text);
    }

    @NotNull
    public static String getString(String text) {
        String[] words = text.toLowerCase().split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}