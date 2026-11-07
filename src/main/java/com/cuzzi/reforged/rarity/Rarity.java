package com.cuzzi.reforged.rarity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum Rarity {
    COMMON("common", ChatFormatting.WHITE),
    UNCOMMON("uncommon", ChatFormatting.GREEN),
    RARE("rare", ChatFormatting.DARK_BLUE),
    EPIC("epic", ChatFormatting.LIGHT_PURPLE),
    UNIQUE("unique", ChatFormatting.RED),
    LEGENDARY("legendary", ChatFormatting.GOLD),
    MYTHIC("mythic", ChatFormatting.DARK_AQUA);

    private final String id;
    private final ChatFormatting color;

    Rarity(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    public ChatFormatting color() {
        return color;
    }

    public Component displayName() {
        return Component.translatable("rarity.reforged." + id).withStyle(color);
    }

    public static Rarity byId(String id) {
        if (id == null) return COMMON;
        for (Rarity rarity : values()) {
            if (rarity.id.equalsIgnoreCase(id.trim())) return rarity;
        }
        return COMMON;
    }
}