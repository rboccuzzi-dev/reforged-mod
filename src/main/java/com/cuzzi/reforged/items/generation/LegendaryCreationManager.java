package com.cuzzi.reforged.items.generation;

import com.cuzzi.reforged.items.definition.ItemDefinition;
import com.cuzzi.reforged.items.legendary.LegendaryNameSavedData;
import com.cuzzi.reforged.items.modifier.CraftingModifier;
import com.cuzzi.reforged.items.pipeline.ItemPipeline;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.network.ClientboundOpenNamingScreenPayload;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.StatsService;
import com.cuzzi.reforged.stats.PlayerStatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LegendaryCreationManager {

    private record PendingCreation(ItemDefinition def, Map<String, String> materials, int itemLevel, ItemRarity rarity, CraftingModifier modifier, float quality, boolean adminBypass) {}

    private static final Map<UUID, PendingCreation> PENDING_CREATIONS = new HashMap<>();

    public static void startCreation(ServerPlayer player, ItemDefinition def, Map<String, String> materials, int itemLevel, ItemRarity rarity, CraftingModifier modifier, float quality, boolean adminBypass) {
        PENDING_CREATIONS.put(player.getUUID(), new PendingCreation(def, materials, itemLevel, rarity, modifier, quality, adminBypass));
        PacketDistributor.sendToPlayer(player, new ClientboundOpenNamingScreenPayload());
    }

    public static void finishCreation(ServerPlayer player, String customName) {
        PendingCreation pending = PENDING_CREATIONS.get(player.getUUID());
        if (pending == null) return;

        LegendaryNameSavedData savedData = LegendaryNameSavedData.get(player.serverLevel());

        if (!savedData.tryRegisterName(customName)) {
            player.sendSystemMessage(Component.literal("The artifact name '" + customName + "' has already been claimed in this world!")
                    .withStyle(ChatFormatting.RED));
            PacketDistributor.sendToPlayer(player, new ClientboundOpenNamingScreenPayload());
            return;
        }

        PENDING_CREATIONS.remove(player.getUUID());

        ItemStack item = ItemPipeline.generate(
                player.server.registryAccess(), pending.def(), pending.materials(), pending.itemLevel(),
                pending.rarity(), pending.modifier(), pending.quality(),
                player.getName().getString(), pending.adminBypass(), customName
        );

        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }

        // Títulos según rareza
        PlayerStatsData playerStats = ModAttachments.stats(player);
        if (playerStats != null) {
            String titleId = (pending.rarity() == ItemRarity.MYTHIC) ? "first_mythic_smith" : "first_legendary_smith";
            if (!playerStats.unlockedTitles().contains(titleId)) {
                playerStats.unlockTitle(titleId);
                StatsService.refresh(player);
            }
        }

        ChatFormatting rarityColor = pending.rarity().color();
        Component announcement = Component.literal("🌟 ")
                .append(player.getName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" has forged the " + pending.rarity().name() + " artifact: ").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(customName).withStyle(rarityColor, ChatFormatting.BOLD))
                .append(Component.literal("!").withStyle(ChatFormatting.YELLOW));

        player.server.getPlayerList().broadcastSystemMessage(announcement, false);
    }
}