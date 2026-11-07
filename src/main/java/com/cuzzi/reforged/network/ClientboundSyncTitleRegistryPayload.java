package com.cuzzi.reforged.network;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.titles.TitleDefinition;
import com.cuzzi.reforged.titles.TitleRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Map;

public record ClientboundSyncTitleRegistryPayload(String id, Component displayName, String rarityId) implements CustomPacketPayload {

    public static final Type<ClientboundSyncTitleRegistryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "sync_title_registry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncTitleRegistryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ClientboundSyncTitleRegistryPayload::id,
            ComponentSerialization.STREAM_CODEC, ClientboundSyncTitleRegistryPayload::displayName,
            ByteBufCodecs.STRING_UTF8, ClientboundSyncTitleRegistryPayload::rarityId,
            ClientboundSyncTitleRegistryPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundSyncTitleRegistryPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ItemRarity rarity = ItemRarity.byId(payload.rarityId());
            TitleDefinition customDef = new TitleDefinition(
                    payload.id(),
                    payload.displayName(),
                    rarity,
                    Map.of(),
                    Map.of(),
                    0,
                    0,
                    List.of()
            );
            TitleRegistry.register(customDef);
        });
    }
}