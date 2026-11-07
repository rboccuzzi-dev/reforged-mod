package com.cuzzi.reforged.server;

import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class StatEffects {

    private static final ResourceLocation HEALTH_ID = id("stat_health_derived");
    private static final ResourceLocation DAMAGE_ID = id("stat_strength_damage");
    private static final ResourceLocation SPEED_ID = id("stat_agility_speed");
    private static final ResourceLocation ATTACK_SPEED_ID = id("stat_agility_attack_speed");

    public static double applySoftCap(double rawValue, double softCap, double efficiencyAboveCap) {
        if (rawValue <= softCap) {
            return rawValue;
        }
        double excess = rawValue - softCap;
        return softCap + (excess * efficiencyAboveCap);
    }

    public static void apply(ServerPlayer player, PlayerStatsData data) {
        int str = data.appliedValue(StatType.STRENGTH) + data.bonus(StatType.STRENGTH, player);
        int agility = data.appliedValue(StatType.AGILITY) + data.bonus(StatType.AGILITY, player);
        int intelligence = data.appliedValue(StatType.INTELLIGENCE) + data.bonus(StatType.INTELLIGENCE, player);

        // --- Derived Health Calculation (Strength Awakening based) ---
        double hpMultiplier = switch (data.awakeningStage()) {
            case 1 -> 3.0;
            case 2 -> 5.0;
            case 3 -> 7.0;
            case 4 -> 9.0;
            default -> 3.0;
        };
        // Subtract 20.0 because Minecraft baseline player health is 20.0 hearts
        double derivedHealth = (str * hpMultiplier);

        // --- Derived Mana Calculation (Intelligence based, locked if Aura active) ---
        int derivedMana = data.appliedValue(StatType.AURA) > 0 ? 0 : 20 + (intelligence * 10);
        data.setCurrentMana(derivedMana);

        // --- Agility Soft Capped Speed ---
        double cappedAgility = applySoftCap(agility, 50.0, 0.30);

        // Apply attributes
        set(player, Attributes.MAX_HEALTH, HEALTH_ID, derivedHealth, AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.ATTACK_DAMAGE, DAMAGE_ID, str * 0.2, AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.MOVEMENT_SPEED, SPEED_ID, cappedAgility * 0.01, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        set(player, Attributes.ATTACK_SPEED, ATTACK_SPEED_ID, cappedAgility * 0.001, AttributeModifier.Operation.ADD_VALUE);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void set(ServerPlayer player, Holder<Attribute> attribute, ResourceLocation id,
                            double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (amount == 0) {
            instance.removeModifier(id);
        } else {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("reforged", path);
    }

    private void StatsEffects() {}
}