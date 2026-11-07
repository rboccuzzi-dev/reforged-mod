package com.cuzzi.reforged.network;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.server.magic.spells.SpellRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundCastSkillPayload(int slot) implements CustomPacketPayload {

    public static final Type<ServerboundCastSkillPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "cast_skill"));

    public static final StreamCodec<ByteBuf, ServerboundCastSkillPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundCastSkillPayload::slot,
            ServerboundCastSkillPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundCastSkillPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                String[] slots = ModAttachments.stats(player).skillSlots();
                int requestedSlot = payload.slot();

                if (requestedSlot >= 0 && requestedSlot < 5) {
                    String skillId = slots[requestedSlot];

                    // Verificamos que el slot no esté vacío
                    if (skillId != null && !skillId.isEmpty()) {
                        Spell spell = SpellRegistry.get(skillId);

                        // Si el hechizo existe en el registro, intentamos castearlo
                        if (spell != null) {
                            spell.cast(player);
                        }
                    }
                }
            }
        });
    }
}