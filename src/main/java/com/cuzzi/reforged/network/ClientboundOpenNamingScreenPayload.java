package com.cuzzi.reforged.network;

import com.cuzzi.reforged.client.gui.LegendaryNamingScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundOpenNamingScreenPayload() implements CustomPacketPayload {
    public static final Type<ClientboundOpenNamingScreenPayload> TYPE = 
            new Type<>(ResourceLocation.fromNamespaceAndPath("reforged", "open_naming_screen"));
    public static final StreamCodec<ByteBuf, ClientboundOpenNamingScreenPayload> STREAM_CODEC = 
            StreamCodec.unit(new ClientboundOpenNamingScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(final ClientboundOpenNamingScreenPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new LegendaryNamingScreen());
        });
    }
}