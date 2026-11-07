package com.cuzzi.reforged.network;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.StatsService;
import com.cuzzi.reforged.stats.PlayerStatsData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record ApplyStatsPayload(Map<String, Integer> allocations) implements CustomPacketPayload {
    public static final Type<ApplyStatsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("reforged", "apply_stats"));
    public static final StreamCodec<ByteBuf, ApplyStatsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), ApplyStatsPayload::allocations,
            ApplyStatsPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final ApplyStatsPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                PlayerStatsData data = ModAttachments.stats(player);
                if (data != null) {
                    data.applyAllocation(payload.allocations());
                    StatsService.refresh(player);
                }
            }
        });
    }
}