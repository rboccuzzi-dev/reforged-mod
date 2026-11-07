package com.cuzzi.reforged.client;

import com.cuzzi.reforged.client.render.RuneRenderer;
import com.cuzzi.reforged.registry.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientInit {

    public static void init(IEventBus modEventBus) {
        // Registros previos (HUD, KeyMappings, etc.)
        modEventBus.addListener(ClientModEvents::registerKeyMapping);
        modEventBus.addListener(StatsHud::registerLayers);

        NeoForge.EVENT_BUS.register(LevelNameplates.class);
        NeoForge.EVENT_BUS.addListener(ClientModEvents::onRenderGuiLayer);

        // REGISTRO DE RENDERIZADORES DE ENTIDADES
        modEventBus.addListener(ClientInit::registerEntityRenderers);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Runa visual
        event.registerEntityRenderer(ModEntities.RUNE.get(), RuneRenderer::new);

        // Entidades lógicas (invisibles, usan NoopRenderer para no crashear)
        event.registerEntityRenderer(ModEntities.SPELL_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.SPELL_FIELD.get(), NoopRenderer::new);
    }

    private ClientInit() {}
}