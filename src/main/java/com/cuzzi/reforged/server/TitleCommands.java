package com.cuzzi.reforged.server;

import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.cuzzi.reforged.network.ClientboundSyncTitleRegistryPayload;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.titles.CustomTitleSavedData;
import com.cuzzi.reforged.titles.TitleDefinition;
import com.cuzzi.reforged.titles.TitleRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TitleCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reforged")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("title")
                        .then(Commands.literal("give")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("title_id", StringArgumentType.string())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(TitleRegistry.REGISTRY.keySet(), builder))
                                                .executes(ctx -> giveTitle(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "title_id")))
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("title_id", StringArgumentType.string())
                                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(TitleRegistry.REGISTRY.keySet(), builder))
                                                .executes(ctx -> removeTitle(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), StringArgumentType.getString(ctx, "title_id")))
                                        )
                                )
                        )
                        .then(Commands.literal("create")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("title_id", StringArgumentType.string())
                                                .then(Commands.argument("rarity", StringArgumentType.string())
                                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                                Arrays.stream(ItemRarity.values()).map(r -> r.name().toLowerCase()), builder))
                                                        .then(Commands.argument("display_name", StringArgumentType.greedyString())
                                                                .executes(ctx -> createCustomTitle(
                                                                        ctx.getSource(),
                                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                                        StringArgumentType.getString(ctx, "title_id"),
                                                                        StringArgumentType.getString(ctx, "rarity"),
                                                                        StringArgumentType.getString(ctx, "display_name")
                                                                ))
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int giveTitle(CommandSourceStack source, Collection<ServerPlayer> targets, String titleId) {
        TitleDefinition def = TitleRegistry.get(titleId);
        if (def == null) {
            source.sendFailure(Component.literal("Title not found: " + titleId));
            return 0;
        }

        for (ServerPlayer player : targets) {
            PlayerStatsData data = ModAttachments.stats(player);
            data.unlockTitle(titleId);
            if (data.displayTitle() == null || data.displayTitle().isEmpty()) {
                data.setDisplayTitle(titleId);
            }
            StatsService.refresh(player);
        }

        source.sendSuccess(() -> Component.literal("Granted title [" + titleId + "] to " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int removeTitle(CommandSourceStack source, Collection<ServerPlayer> targets, String titleId) {
        for (ServerPlayer player : targets) {
            PlayerStatsData data = ModAttachments.stats(player);
            data.removeTitle(titleId);
            StatsService.refresh(player);
        }

        source.sendSuccess(() -> Component.literal("Removed title [" + titleId + "] from " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int createCustomTitle(CommandSourceStack source, Collection<ServerPlayer> targets, String titleId, String rarityStr, String displayNameText) {
        ItemRarity rarity = ItemRarity.byId(rarityStr);
        Component displayName = Component.literal(displayNameText);

        TitleDefinition customTitle = new TitleDefinition(
                titleId,
                displayName,
                rarity,
                Map.of(),
                Map.of(),
                0,
                0,
                List.of()
        );

        TitleRegistry.register(customTitle);

        CustomTitleSavedData savedData = CustomTitleSavedData.get(source.getServer().overworld());
        savedData.setDirty();

        PacketDistributor.sendToAllPlayers(new ClientboundSyncTitleRegistryPayload(titleId, displayName, rarity.name()));

        for (ServerPlayer player : targets) {
            PlayerStatsData data = ModAttachments.stats(player);
            data.unlockTitle(titleId);
            if (data.displayTitle() == null || data.displayTitle().isEmpty()) {
                data.setDisplayTitle(titleId);
            }
            StatsService.refresh(player);
        }

        source.sendSuccess(() -> Component.literal("Created and granted custom title [" + displayNameText + "] (" + rarity.name() + ") to " + targets.size() + " player(s)."), true);
        return targets.size();
    }
}