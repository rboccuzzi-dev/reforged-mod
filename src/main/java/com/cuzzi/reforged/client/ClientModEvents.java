package com.cuzzi.reforged.client;

import com.cuzzi.reforged.network.ServerboundCastSkillPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ClientModEvents {

    public static final KeyMapping[] SKILL_KEYS = new KeyMapping[5];

    // Inicializar en ClientModEvents (Ej: Teclas Z, X, C, V, B o números)
    static {
        for (int i = 0; i < 5; i++) {
            SKILL_KEYS[i] = new KeyMapping("key.reforged.skill_" + (i+1),
                    InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN, "key.categories.reforged");
        }
    }

    // Dentro del ClientTickEvent.Post (Igual que como abres StatsScreen)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        for (int i = 0; i < 5; i++) {
            while (SKILL_KEYS[i].consumeClick()) {
                PacketDistributor.sendToServer(new ServerboundCastSkillPayload(i));
            }
        }
    }

    public static final KeyMapping OPEN_STATS =
            new KeyMapping(
                    "key.reforged.open_stats",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_K,
                    "key.categories.reforged"
            );

    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STATS);
    }

    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH) || event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);
        }
    }
}