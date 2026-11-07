package com.cuzzi.reforged.registry;

// Notice the new import path here:
import com.cuzzi.reforged.items.components.ReforgedItemData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "reforged");

    public static final Supplier<DataComponentType<ReforgedItemData>> REFORGED_ITEM_DATA =
            DATA_COMPONENT_TYPES.register("item_data", () ->
                    DataComponentType.<ReforgedItemData>builder()
                            .persistent(ReforgedItemData.CODEC)
                            .networkSynchronized(ReforgedItemData.STREAM_CODEC)
                            .build());

    private ModDataComponents() {}
}