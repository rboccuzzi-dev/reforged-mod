package com.cuzzi.reforged.titles;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.util.AuxFunctions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Reforged.MODID)
public class TitleEvents {

    @SubscribeEvent
    public static void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Component prefix = AuxFunctions.buildPlayerPrefix(player);
            event.setDisplayname(Component.empty()
                    .append(prefix)
                    .append(player.getName()));
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        Component fullMessage = AuxFunctions.formatChatMessage(player, event.getRawText());

        player.server.getPlayerList().broadcastSystemMessage(fullMessage, false);
        event.setCanceled(true);
    }
}