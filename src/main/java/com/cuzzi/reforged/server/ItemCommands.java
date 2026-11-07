package com.cuzzi.reforged.server;

import com.cuzzi.reforged.items.definition.ItemDefinition;
import com.cuzzi.reforged.items.generation.LegendaryCreationManager;
import com.cuzzi.reforged.items.modifier.CraftingModifier;
import com.cuzzi.reforged.items.pipeline.ItemPipeline;
import com.cuzzi.reforged.items.rarity.ItemRarity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.Map;

public class ItemCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reforged")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("item")
                .then(Commands.literal("create")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("item_id", StringArgumentType.string())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ItemDefinition.REGISTRY.keySet(), builder))
                            .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                                .then(Commands.argument("rarity", StringArgumentType.string())
                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                            Arrays.stream(ItemRarity.values()).map(r -> r.name().toLowerCase()), builder))
                                    .executes(ctx -> createItem(ctx, "none", 1.0f))
                                    .then(Commands.argument("modifier", StringArgumentType.string())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(CraftingModifier.values()).map(m -> m.name().toLowerCase()), builder))
                                        .executes(ctx -> createItem(ctx, StringArgumentType.getString(ctx, "modifier"), 1.0f))
                                        .then(Commands.argument("quality", FloatArgumentType.floatArg(0.0f, 1.0f))
                                            .executes(ctx -> createItem(ctx, StringArgumentType.getString(ctx, "modifier"), FloatArgumentType.getFloat(ctx, "quality")))
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ));
    }

    private static int createItem(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String modifierId, float quality) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        String itemId = StringArgumentType.getString(ctx, "item_id");
        int level = IntegerArgumentType.getInteger(ctx, "level");
        String rarityId = StringArgumentType.getString(ctx, "rarity");

        ItemDefinition def = ItemDefinition.REGISTRY.get(itemId);
        if (def == null) {
            ctx.getSource().sendFailure(Component.literal("Item definition not found: " + itemId));
            return 0;
        }

        ItemRarity rarity = ItemRarity.byId(rarityId);
        CraftingModifier modifier = CraftingModifier.byId(modifierId);
        Map<String, String> materials = Map.of("MAIN", "reforged:iron");

        if (rarity == ItemRarity.LEGENDARY) {
            LegendaryCreationManager.startCreation(target, def, materials, level, rarity, modifier, quality, true);
            ctx.getSource().sendSuccess(() -> Component.literal("Started Legendary creation (Lv. " + level + ") for " + target.getName().getString()), true);
        } else {
            ItemStack item = ItemPipeline.generate(
                    ctx.getSource().registryAccess(), def, materials, level, rarity, modifier, quality,
                    ctx.getSource().getTextName(), true, null
            );

            if (!target.getInventory().add(item)) {
                target.drop(item, false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("Generated Lv. " + level + " " + rarity.name() + " " + itemId), true);
        }
        return 1;
    }
}