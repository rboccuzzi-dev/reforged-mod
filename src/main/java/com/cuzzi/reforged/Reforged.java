package com.cuzzi.reforged;

import com.cuzzi.reforged.registry.ModEntities;
import com.cuzzi.reforged.server.ItemCommands;
import com.cuzzi.reforged.server.MobLevels;
import com.cuzzi.reforged.server.StatsInit;
import com.cuzzi.reforged.server.TitleCommands;
import com.cuzzi.reforged.titles.CustomTitleSavedData;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(Reforged.MODID)
public class Reforged {

    public static final String MODID = "reforged";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public Reforged(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        StatsInit.init(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);


        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        MobLevels.registerDefault(() -> EntityType.ZOMBIE, 10, 25);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Reforged RPG Core initialized successfully.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ItemCommands.register(event.getDispatcher());
        TitleCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Reforged server systems active.");
        CustomTitleSavedData.get(event.getServer().overworld());
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Reforged Client initialized.");
        }
    }

}