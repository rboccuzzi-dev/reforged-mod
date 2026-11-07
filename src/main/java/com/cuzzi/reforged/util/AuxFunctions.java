package com.cuzzi.reforged.util;

import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.rarity.Rarity;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import com.cuzzi.reforged.titles.TitleDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public final class AuxFunctions {

    private AuxFunctions() {}

    /**
     * Dibuja un panel contenedor estandarizado con fondo y borde.
     */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height, int borderColor, int backgroundColor) {
        graphics.fill(x, y, x + width, y + height, backgroundColor);
        graphics.renderOutline(x, y, width, height, borderColor);
    }

    /**
     * Capitaliza la primera letra de un texto.
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Formatea el identificador o clave de traducción de una estadística a un nombre legible.
     */
    public static String formatStatName(StatType type) {
        if (type == null) return "";
        String key = type.displayName().getString();
        if (key.startsWith("stat.")) {
            String[] parts = key.split("\\.");
            String raw = parts[parts.length - 1];
            return capitalize(raw);
        }
        return key;
    }

    /**
     * Prioridad numérica de rareza para ordenamientos (de mayor a menor).
     */
    public static int getRarityPriority(ItemRarity rarity) {
        if (rarity == null) return 0;
        return switch (rarity) {
            case MYTHIC -> 6;
            case LEGENDARY -> 5;
            case UNIQUE -> 4;
            case EPIC -> 3;
            case RARE -> 2;
            case UNCOMMON -> 1;
            case COMMON -> 0;
        };
    }

    public static int getRarityPriority(Rarity rarity) {
        if (rarity == null) return 0;
        return switch (rarity) {
            case MYTHIC -> 6;
            case LEGENDARY -> 5;
            case UNIQUE -> 4;
            case EPIC -> 3;
            case RARE -> 2;
            case UNCOMMON -> 1;
            case COMMON -> 0;
        };
    }

    /**
     * Construye el componente de prefijo de jugador: "[LV. X] [TITULO] "
     */
    public static Component buildPlayerPrefix(ServerPlayer player) {
        PlayerStatsData data = ModAttachments.stats(player);
        MutableComponent prefix = Component.empty();

        int level = (data != null) ? data.level() : 1;
        prefix.append(Component.literal("[LV. " + level + "] ").withStyle(ChatFormatting.GOLD));

        if (data != null) {
            TitleDefinition displayTitle = data.displayTitleDefinition();
            if (displayTitle != null) {
                prefix.append(Component.literal("[")
                        .append(displayTitle.displayName())
                        .append("] ")
                        .withStyle(displayTitle.rarity().color()));
            }
        }

        return prefix;
    }

    /**
     * Formatea una línea completa de chat: "[LV. X] [TITULO] Jugador >> Mensaje"
     */
    public static Component formatChatMessage(ServerPlayer player, String messageText) {
        Component prefix = buildPlayerPrefix(player);
        return Component.empty()
                .append(prefix)
                .append(player.getName().copy().withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" >> ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(messageText).withStyle(ChatFormatting.WHITE));
    }
}