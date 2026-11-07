package com.cuzzi.reforged.network;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.StatsService;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundAssignSkillPayload(int slot, String skillId) implements CustomPacketPayload {

    public static final Type<ServerboundAssignSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "assign_skill"));

    public static final StreamCodec<ByteBuf, ServerboundAssignSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundAssignSkillPayload::slot,
            ByteBufCodecs.STRING_UTF8, ServerboundAssignSkillPayload::skillId,
            ServerboundAssignSkillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundAssignSkillPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                // Asignamos la habilidad al slot correspondiente (0 a 4)
                ModAttachments.stats(player).assignSkill(payload.slot(), payload.skillId());

                // Refrescamos los stats para que el cliente actualice el HUD inmediatamente
                StatsService.refresh(player);
            }
        });
    }
}