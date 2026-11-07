package com.cuzzi.reforged.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/**
 * One level range, loaded from data/<namespace>/reforged/mob_level/<name>.json
 *
 * {
 *   "entities": ["minecraft:zombie", "mymod:my_mob", "#mymod:bosses"],
 *   "min": 10,
 *   "max": 15,
 *   "priority": 0        // optional, the highest priority wins when several entries match a mob
 * }
 */
public record MobLevelEntry(HolderSet<EntityType<?>> entities, int min, int max, int priority) {

    public static final ResourceKey<Registry<MobLevelEntry>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("reforged", "mob_level"));

    public static final Codec<MobLevelEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(MobLevelEntry::entities),
            ExtraCodecs.POSITIVE_INT.fieldOf("min").forGetter(MobLevelEntry::min),
            ExtraCodecs.POSITIVE_INT.fieldOf("max").forGetter(MobLevelEntry::max),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(MobLevelEntry::priority)
    ).apply(inst, MobLevelEntry::new));

    /** Server-only datapack registry (nothing is sent to clients). Hooked from StatsInit on the mod bus. */
    public static void registerDatapackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY_KEY, CODEC);
    }
}