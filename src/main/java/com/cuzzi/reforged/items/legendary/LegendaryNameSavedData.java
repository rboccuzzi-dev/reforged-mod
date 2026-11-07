package com.cuzzi.reforged.items.legendary;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class LegendaryNameSavedData extends SavedData {
    private static final String FILE_NAME = "reforged_legendary_names";
    private final Set<String> usedNames = new HashSet<>();

    public static Factory<LegendaryNameSavedData> factory() {
        return new Factory<>(
                LegendaryNameSavedData::new,
                LegendaryNameSavedData::load,
                null
        );
    }

    public static LegendaryNameSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(factory(), FILE_NAME);
    }

    public boolean isNameUsed(String name) {
        return usedNames.contains(name.trim().toLowerCase());
    }

    public boolean tryRegisterName(String name) {
        String clean = name.trim().toLowerCase();
        if (usedNames.contains(clean)) {
            return false;
        }
        usedNames.add(clean);
        setDirty();
        return true;
    }

    public static LegendaryNameSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        LegendaryNameSavedData data = new LegendaryNameSavedData();
        ListTag list = tag.getList("names", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            data.usedNames.add(list.getString(i).toLowerCase());
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (String name : usedNames) {
            list.add(StringTag.valueOf(name));
        }
        tag.put("names", list);
        return tag;
    }
}