package com.cuzzi.reforged.network;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncStatsPayload(PlayerStatsData statsData) implements CustomPacketPayload {
    public static final Type<SyncStatsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("reforged", "sync_stats"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(PlayerStatsData.CODEC), SyncStatsPayload::statsData,
            SyncStatsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final SyncStatsPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            PlayerStatsData clientStats = ModAttachments.stats(ctx.player());
            if (clientStats != null) {
                clientStats.copyFrom(payload.statsData());
            }
        });
    }
}