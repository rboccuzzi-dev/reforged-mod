package com.cuzzi.reforged.items.rarity;

import net.minecraft.ChatFormatting;

public enum ItemRarity {
    COMMON("common", 1.0f, ChatFormatting.WHITE),
    UNCOMMON("uncommon", 1.2f, ChatFormatting.GREEN),
    RARE("rare", 1.35f, ChatFormatting.DARK_BLUE),
    EPIC("epic", 1.5f, ChatFormatting.LIGHT_PURPLE),
    UNIQUE("unique", 2.0f, ChatFormatting.RED),
    LEGENDARY("legendary", 3.0f, ChatFormatting.GOLD),
    MYTHIC("mythic", 6.0f, ChatFormatting.DARK_AQUA);

    private final String id;
    private final float multiplier;
    private final ChatFormatting color;

    ItemRarity(String id, float multiplier, ChatFormatting color) {
        this.id = id;
        this.multiplier = multiplier;
        this.color = color;
    }

    public boolean requiresNaming() {
        return this == LEGENDARY || this == MYTHIC;
    }

    public float multiplier() { return multiplier; }
    public ChatFormatting color() { return color; }

    public static ItemRarity byId(String id) {
        for (ItemRarity r : values()) {
            if (r.id.equalsIgnoreCase(id)) return r;
        }
        return COMMON;
    }
}