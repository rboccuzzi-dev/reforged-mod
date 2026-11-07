package com.cuzzi.reforged.items.material;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record MaterialDefinition(
        int tier,
        int maxPotentialLevel,
        double damageMultiplier,
        double durabilityMultiplier,
        double speedMultiplier,
        boolean isCelestial // Para el futuro sistema Mythic
) {
    // La clave del registro para los Datapacks
    public static final ResourceKey<Registry<MaterialDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("reforged", "materials"));

    // El Codec que traduce el JSON a esta clase Java
    public static final Codec<MaterialDefinition> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("tier", 1).forGetter(MaterialDefinition::tier),
            Codec.INT.optionalFieldOf("max_potential_level", 10).forGetter(MaterialDefinition::maxPotentialLevel),
            Codec.DOUBLE.optionalFieldOf("damage_multiplier", 1.0).forGetter(MaterialDefinition::damageMultiplier),
            Codec.DOUBLE.optionalFieldOf("durability_multiplier", 1.0).forGetter(MaterialDefinition::durabilityMultiplier),
            Codec.DOUBLE.optionalFieldOf("speed_multiplier", 1.0).forGetter(MaterialDefinition::speedMultiplier),
            Codec.BOOL.optionalFieldOf("is_celestial", false).forGetter(MaterialDefinition::isCelestial)
    ).apply(inst, MaterialDefinition::new));
}