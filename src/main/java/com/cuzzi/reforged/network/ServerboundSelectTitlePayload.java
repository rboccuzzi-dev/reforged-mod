package com.cuzzi.reforged.network;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.StatsService;
import com.cuzzi.reforged.stats.PlayerStatsData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSelectTitlePayload(String titleId, boolean setDisplay) implements CustomPacketPayload {

    public static final Type<ServerboundSelectTitlePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "select_title"));

    public static final StreamCodec<FriendlyByteBuf, ServerboundSelectTitlePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundSelectTitlePayload::titleId,
            ByteBufCodecs.BOOL, ServerboundSelectTitlePayload::setDisplay,
            ServerboundSelectTitlePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundSelectTitlePayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                PlayerStatsData data = ModAttachments.stats(player);
                if (data != null) {
                    if (payload.setDisplay()) {
                        data.setDisplayTitle(payload.titleId());
                    } else {
                        if (payload.titleId().equals(data.activeTitle())) {
                            data.setActiveTitle("");
                        } else {
                            data.setActiveTitle(payload.titleId());
                        }
                    }
                    StatsService.refresh(player);
                }
            }
        });
    }
}