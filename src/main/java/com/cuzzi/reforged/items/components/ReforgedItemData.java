package com.cuzzi.reforged.items.components;

import com.cuzzi.reforged.items.modifier.CraftingModifier;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.UUID;

public record ReforgedItemData(
        int version, // For future data migration (e.g., if we rename a stat)
        UUID uniqueId,
        String definitionId,
        Map<String, String> materials, // e.g., "MAIN" -> "iron", "TERTIARY" -> "oak"
        int itemLevel,
        int requiredLevel,
        ItemRarity rarity,
        CraftingModifier modifier,
        float quality,
        String creatorName,
        boolean isAdminCreation,
        Map<String, Double> finalVanillaStats,
        Map<String, Double> finalCustomStats
) {
    public static final Codec<ReforgedItemData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("version").forGetter(ReforgedItemData::version),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("unique_id").forGetter(ReforgedItemData::uniqueId),
            Codec.STRING.fieldOf("definition").forGetter(ReforgedItemData::definitionId),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("materials").forGetter(ReforgedItemData::materials),
            Codec.INT.fieldOf("item_level").forGetter(ReforgedItemData::itemLevel),
            Codec.INT.fieldOf("required_level").forGetter(ReforgedItemData::requiredLevel),
            Codec.STRING.xmap(ItemRarity::byId, ItemRarity::name).fieldOf("rarity").forGetter(ReforgedItemData::rarity),
            Codec.STRING.xmap(CraftingModifier::byId, CraftingModifier::name).fieldOf("modifier").forGetter(ReforgedItemData::modifier),
            Codec.FLOAT.fieldOf("quality").forGetter(ReforgedItemData::quality),
            Codec.STRING.fieldOf("creator").forGetter(ReforgedItemData::creatorName),
            Codec.BOOL.fieldOf("is_admin").forGetter(ReforgedItemData::isAdminCreation),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("vanilla_stats").forGetter(ReforgedItemData::finalVanillaStats),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("custom_stats").forGetter(ReforgedItemData::finalCustomStats)
    ).apply(inst, ReforgedItemData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReforgedItemData> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
}