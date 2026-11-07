package com.cuzzi.reforged.client;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.client.gui.StatsScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(
        modid = Reforged.MODID,
        value = Dist.CLIENT

)
public class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (ClientModEvents.OPEN_STATS.consumeClick()) {
            if (mc.player != null) {
                mc.setScreen(new StatsScreen());
            }
        }
    }
}
