package com.cuzzi.reforged.network;

import com.cuzzi.reforged.items.generation.LegendaryCreationManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSubmitLegendaryNamePayload(String name) implements CustomPacketPayload {

    public static final Type<ServerboundSubmitLegendaryNamePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("reforged", "submit_legendary_name"));

    public static final StreamCodec<ByteBuf, ServerboundSubmitLegendaryNamePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundSubmitLegendaryNamePayload::name,
            ServerboundSubmitLegendaryNamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final ServerboundSubmitLegendaryNamePayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                LegendaryCreationManager.finishCreation(player, payload.name());
            }
        });
    }
}