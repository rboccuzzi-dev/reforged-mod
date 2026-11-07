package com.cuzzi.reforged.server;

import com.cuzzi.reforged.registry.ModAttachments;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class MobLevels {

    public record Range(int min, int max) {}

    private static final ResourceLocation HEALTH_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_health");
    private static final ResourceLocation DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_damage");
    private static final ResourceLocation ARMOR_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_armor");
    private static final ResourceLocation TOUGHNESS_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_toughness");
    private static final ResourceLocation KB_RESIST_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_kb_resist");
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("reforged", "mob_level_speed");

    private record CodeDefault(Supplier<? extends EntityType<?>> type, Range range) {}
    private static final List<CodeDefault> CODE_DEFAULTS = new ArrayList<>();

    public static void registerDefault(Supplier<? extends EntityType<?>> type, int min, int max) {
        CODE_DEFAULTS.add(new CodeDefault(type, new Range(min, Math.max(min, max))));
    }

    public static double multiplier(int level) {
        return Math.pow(1.035, Math.max(1, level) - 1);
    }

    public static Range rangeFor(Mob mob) {
        EntityType<?> type = mob.getType();

        Optional<Registry<MobLevelEntry>> registry = mob.level().registryAccess().registry(MobLevelEntry.REGISTRY_KEY);
        if (registry.isPresent()) {
            MobLevelEntry best = null;
            Holder<EntityType<?>> typeHolder = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type);

            for (MobLevelEntry entry : registry.get()) {
                if (entry.entities().contains(typeHolder)
                        && (best == null || entry.priority() > best.priority())) {
                    best = entry;
                }
            }
            if (best != null) {
                return new Range(best.min(), Math.max(best.min(), best.max()));
            }
        }

        if (mob instanceof LevelledMob levelled) {
            return new Range(levelled.minLevel(), Math.max(levelled.minLevel(), levelled.maxLevel()));
        }

        for (CodeDefault codeDefault : CODE_DEFAULTS) {
            if (codeDefault.type().get() == type) return codeDefault.range();
        }

        return switch (type.getCategory()) {
            case MONSTER -> new Range(5, 10);
            case CREATURE, AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, WATER_AMBIENT, AXOLOTLS ->
                    new Range(1, 3);
            default -> new Range(1, 5);
        };
    }

    public static void ensureLevel(Mob mob) {
        if (mob.getData(ModAttachments.MOB_LEVEL) > 0) return;

        Range range = rangeFor(mob);
        int level = range.min() >= range.max()
                ? range.min()
                : mob.getRandom().nextIntBetweenInclusive(range.min(), range.max());
        setLevel(mob, level, true);
    }

    public static void setLevel(Mob mob, int level, boolean healToFull) {
        level = Math.max(1, level);
        mob.setData(ModAttachments.MOB_LEVEL, level);

        double hpMultiplier = multiplier(level);
        scale(mob, Attributes.MAX_HEALTH, HEALTH_ID, hpMultiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        double dmgMultiplier = Math.pow(1.03, level - 1);
        scale(mob, Attributes.ATTACK_DAMAGE, DAMAGE_ID, dmgMultiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        double armorBonus = Math.min(30.0, (level - 1) * 0.2);
        scale(mob, Attributes.ARMOR, ARMOR_ID, armorBonus, AttributeModifier.Operation.ADD_VALUE);

        double toughnessBonus = Math.min(20.0, (level - 1) * 0.15);
        scale(mob, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_ID, toughnessBonus, AttributeModifier.Operation.ADD_VALUE);

        double kbResist = Math.min(1.0, level * 0.0075);
        scale(mob, Attributes.KNOCKBACK_RESISTANCE, KB_RESIST_ID, kbResist, AttributeModifier.Operation.ADD_VALUE);

        double speedBonus = Math.min(0.20, level * 0.001);
        scale(mob, Attributes.MOVEMENT_SPEED, SPEED_ID, speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        if (healToFull) {
            mob.setHealth(mob.getMaxHealth());
        }
    }

    private static void scale(Mob mob, Holder<Attribute> attribute, ResourceLocation id, double value, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) return;
        instance.addOrReplacePermanentModifier(new AttributeModifier(id, value, operation));
    }

    private MobLevels() {}
}