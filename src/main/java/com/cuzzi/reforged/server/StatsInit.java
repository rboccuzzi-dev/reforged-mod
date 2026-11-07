package com.cuzzi.reforged.server;

import com.cuzzi.reforged.client.ClientInit;
import com.cuzzi.reforged.items.integration.ItemStatBonusSource;
import com.cuzzi.reforged.network.ModNetwork;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.registry.ModDataComponents;
import com.cuzzi.reforged.titles.TitleStatBonusSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

public final class StatsInit {

    /** Call once from your main mod constructor: StatsInit.init(modEventBus); */
    public static void init(IEventBus modEventBus) {
        ModAttachments.ATTACHMENTS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
        modEventBus.addListener(MobLevelEntry::registerDatapackRegistry);

        modEventBus.addListener((net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry event) -> {
            event.dataPackRegistry(com.cuzzi.reforged.items.material.MaterialDefinition.REGISTRY_KEY,
                    com.cuzzi.reforged.items.material.MaterialDefinition.CODEC);
        });

        ItemStatBonusSource.register();
        TitleStatBonusSource.register();

        NeoForge.EVENT_BUS.register(StatsEvents.class);



        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init(modEventBus);
        }
    }

    private StatsInit() {}
}