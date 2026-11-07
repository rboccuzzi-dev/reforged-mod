package com.cuzzi.reforged.titles;

import com.cuzzi.reforged.items.rarity.ItemRarity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;
import java.util.Map;

public class CustomTitleSavedData extends SavedData {

    private static final String FILE_NAME = "reforged_custom_titles";

    public static CustomTitleSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new Factory<>(CustomTitleSavedData::new, CustomTitleSavedData::load, null),
                FILE_NAME
        );
    }

    public static CustomTitleSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CustomTitleSavedData data = new CustomTitleSavedData();
        ListTag list = tag.getList("titles", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag titleTag = list.getCompound(i);
            String id = titleTag.getString("id");
            String jsonName = titleTag.getString("displayName");
            String rarityId = titleTag.getString("rarity");

            Component displayName = Component.Serializer.fromJson(jsonName, provider);
            if (displayName == null) {
                displayName = Component.literal(id);
            }
            ItemRarity rarity = ItemRarity.byId(rarityId);

            TitleDefinition customDef = new TitleDefinition(
                    id,
                    displayName,
                    rarity,
                    Map.of(),
                    Map.of(),
                    0,
                    0,
                    List.of()
            );
            TitleRegistry.register(customDef);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (TitleDefinition def : TitleRegistry.REGISTRY.values()) {
            if (TitleRegistry.isCustomTitle(def.id())) {
                CompoundTag titleTag = new CompoundTag();
                titleTag.putString("id", def.id());
                titleTag.putString("displayName", Component.Serializer.toJson(def.displayName(), provider));
                titleTag.putString("rarity", def.rarity().name());
                list.add(titleTag);
            }
        }
        tag.put("titles", list);
        return tag;
    }
}