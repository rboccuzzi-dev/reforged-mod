package com.cuzzi.reforged.server;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.magic.RuneEntity;
import com.cuzzi.reforged.server.magic.spells.impl.FireballSpell;
import com.cuzzi.reforged.server.magic.spells.impl.FrostNovaSpell;
import com.cuzzi.reforged.server.magic.spells.impl.HealingSanctuarySpell;
import com.cuzzi.reforged.stats.ClassDefinition;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * /reforged class   <players> set <class>
 * /reforged points  <players> add|set <amount>
 * /reforged level   <players> set <level>
 * /reforged xp      <players> add <amount>
 * /reforged stat    <players> <stat> add|set <points>
 * /reforged reset   <players>
 * /reforged info    <player>
 * /reforged mob level get <entity>
 * /reforged mob level set <entities> <level>
 */
public final class StatsCommands {

    private static final SuggestionProvider<CommandSourceStack> CLASSES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(ClassDefinition.ALL.stream().map(ClassDefinition::id), builder);

    private static final SuggestionProvider<CommandSourceStack> STATS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(Arrays.stream(StatType.values()).map(StatType::id), builder);

    private static final SuggestionProvider<CommandSourceStack> SKILLS = (ctx, builder) ->
            SharedSuggestionProvider.suggest(com.cuzzi.reforged.server.magic.spells.SpellRegistry.getAll().stream().map(com.cuzzi.reforged.server.magic.spells.Spell::getId), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reforged")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("class")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("set")
                                        .then(Commands.argument("class", StringArgumentType.word())
                                                .suggests(CLASSES)
                                                .executes(StatsCommands::setClass)))))

                .then(Commands.literal("points")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(ctx -> each(ctx, "Points added",
                                                        (p, d) -> d.addPoints(IntegerArgumentType.getInteger(ctx, "amount"))))))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> each(ctx, "Points set",
                                                        (p, d) -> d.setAvailablePoints(IntegerArgumentType.getInteger(ctx, "amount"))))))))

                .then(Commands.literal("level")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("set")
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                .executes(ctx -> each(ctx, "Level set",
                                                        (p, d) -> d.setLevel(IntegerArgumentType.getInteger(ctx, "level"))))))))

                .then(Commands.literal("xp")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .executes(ctx -> each(ctx, "XP added",
                                                        (p, d) -> d.addXp(IntegerArgumentType.getInteger(ctx, "amount"))))))))

                .then(Commands.literal("stat")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("stat", StringArgumentType.word())
                                        .suggests(STATS)
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("points", IntegerArgumentType.integer())
                                                        .executes(ctx -> modifyStat(ctx, false))))
                                        .then(Commands.literal("set")
                                                .then(Commands.argument("points", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> modifyStat(ctx, true)))))))

                .then(Commands.literal("reset")
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(ctx -> each(ctx, "Stats reset (points refunded)",
                                        (p, d) -> d.refundAll()))))

                .then(Commands.literal("info")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(StatsCommands::info)))

                .then(Commands.literal("mob")
                        .then(Commands.literal("level")
                                .then(Commands.literal("get")
                                        .then(Commands.argument("entity", EntityArgument.entity())
                                                .executes(StatsCommands::mobLevelGet)))
                                .then(Commands.literal("set")
                                        .then(Commands.argument("entities", EntityArgument.entities())
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1, 1000))
                                                        .executes(StatsCommands::mobLevelSet))))))
                .then(Commands.literal("rune")
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("speed", FloatArgumentType.floatArg(-50f, 50f))
                                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.1f, 20f))
                                                .then(Commands.argument("lifespan_ticks", IntegerArgumentType.integer(10))
                                                        .then(Commands.argument("texture", StringArgumentType.string())
                                                                .then(Commands.argument("color_hex", StringArgumentType.string())
                                                                        .executes(ctx -> {
                                                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                                            float speed = FloatArgumentType.getFloat(ctx, "speed");
                                                                            float scale = FloatArgumentType.getFloat(ctx, "scale");
                                                                            int lifespan = IntegerArgumentType.getInteger(ctx, "lifespan_ticks");
                                                                            String texture = StringArgumentType.getString(ctx, "texture");
                                                                            String hexStr = StringArgumentType.getString(ctx, "color_hex").replace("#", "");

                                                                            int color = 0xFFFFFF;
                                                                            try { color = Integer.parseInt(hexStr, 16); } catch (NumberFormatException ignored) {}

                                                                            // Utilizamos Pitch y Yaw del jugador para alinear la runa perfectamente frente a sus ojos
                                                                            RuneEntity rune = new RuneEntity(player.level(), player.getX(), player.getY() + 1.5, player.getZ(), player.getXRot(), player.getYRot(), scale, speed, lifespan, texture, color);
                                                                            player.level().addFreshEntity(rune);

                                                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Runa Creada!"), true);
                                                                            return 1;
                                                                        })
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("stack")
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .then(Commands.argument("spacing", FloatArgumentType.floatArg(0.1f, 10f))
                                                .then(Commands.argument("color_hex", StringArgumentType.string())
                                                        .executes(ctx -> {
                                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                                            int count = IntegerArgumentType.getInteger(ctx, "count");
                                                            float spacing = FloatArgumentType.getFloat(ctx, "spacing");
                                                            String hexStr = StringArgumentType.getString(ctx, "color_hex").replace("#", "");

                                                            int color = 0x00FF88; // Por defecto Turquesa mágico
                                                            try { color = Integer.parseInt(hexStr, 16); } catch (NumberFormatException ignored) {}

                                                            // Invoca 5 runas espaciadas, que van creciendo (+0.5 de escala por paso) y giran a 5 grados/tick
                                                            RuneEntity.summonRuneStack(player, count, spacing, 1.0f, 0.5f, 5.0f, 200, "reforged:textures/entity/rune/placeholder_rune.png", color);

                                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("Túnel de Runas Creado!"), true);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("cast")
                        .then(Commands.literal("fireball")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean success = new FireballSpell().cast(player);
                                    if (!success) player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c¡No tienes suficiente Maná para Fireball!"));
                                    return 1;
                                })
                        )
                        .then(Commands.literal("healing")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean success = new HealingSanctuarySpell().cast(player);
                                    if (!success) player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c¡No tienes suficiente Maná para Healing!"));
                                    return 1;
                                })
                        )
                        .then(Commands.literal("frost")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean success = new FrostNovaSpell().cast(player);
                                    if (!success) player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c¡No tienes suficiente Maná para Frost Nova!"));
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("skill")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("unlock")
                                        .then(Commands.argument("skill", StringArgumentType.word())
                                                .suggests(SKILLS)
                                                .executes(ctx -> {
                                                    String skillId = StringArgumentType.getString(ctx, "skill");
                                                    return each(ctx, "Skill unlocked: " + skillId,
                                                            (p, d) -> d.unlockSkill(skillId));
                                                })
                                        )
                                )
                        )
                )
        );

    }

    // ---- Player commands ----------------------------------------------------

    /** Runs an action on every selected player, then re-applies attributes and syncs them. */
    private static int each(CommandContext<CommandSourceStack> ctx, String message,
                            BiConsumer<ServerPlayer, PlayerStatsData> action) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
        for (ServerPlayer player : players) {
            action.accept(player, ModAttachments.stats(player));
            StatsService.refresh(player);
        }
        ctx.getSource().sendSuccess(() -> Component.literal(message + " (" + players.size() + " player(s))"), true);
        return players.size();
    }

    private static int setClass(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "class");
        ClassDefinition definition = ClassDefinition.ALL.stream()
                .filter(c -> c.id().equals(id)).findFirst().orElse(null);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown class: " + id));
            return 0;
        }
        return each(ctx, "Class set to " + id + " (applied points were refunded)",
                (p, d) -> d.setPlayerClass(definition));
    }

    private static int modifyStat(CommandContext<CommandSourceStack> ctx, boolean set) throws CommandSyntaxException {
        String id = StringArgumentType.getString(ctx, "stat");
        StatType type = StatType.byId(id);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown stat: " + id));
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(ctx, "points");
        return each(ctx, "Stat " + id + (set ? " set" : " changed"),
                (p, d) -> d.setCommitted(type, set ? amount : d.committed(type) + amount));
    }

    private static int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        PlayerStatsData data = ModAttachments.stats(player);

        List<String> lines = new ArrayList<>();
        lines.add(player.getName().getString() + " - class " + data.playerClass().id()
                + " (" + data.playerClass().rarity().name().toLowerCase() + ")");
        lines.add("Level " + data.level() + ", XP " + data.xp() + "/" + data.xpToNext()
                + ", points to spend: " + data.remainingPoints());
        for (StatType type : data.visibleStats()) {
            lines.add("  " + type.id() + ": " + data.baseValue(type) + " (+" + data.bonus(type, player)
                    + ")  [applied points: " + data.committed(type) + "]");
        }
        ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n", lines)), false);
        return 1;
    }

    // ---- Mob commands ---------------------------------------------------------

    private static int mobLevelGet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(ctx, "entity");
        if (!(entity instanceof Mob mob)) {
            ctx.getSource().sendFailure(Component.literal("That entity is not a mob"));
            return 0;
        }
        int level = mob.getData(ModAttachments.MOB_LEVEL);
        ctx.getSource().sendSuccess(() -> Component.empty()
                .append(mob.getDisplayName())
                .append(" is level " + level + " (stats x" + MobLevels.multiplier(level) + ")"), false);
        return level;
    }

    private static int mobLevelSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        int count = 0;
        for (Entity entity : EntityArgument.getEntities(ctx, "entities")) {
            if (entity instanceof Mob mob) {
                MobLevels.setLevel(mob, level, true);
                count++;
            }
        }
        int total = count;
        ctx.getSource().sendSuccess(() -> Component.literal("Level " + level + " set on " + total + " mob(s)"), true);
        return count;
    }

    private StatsCommands() {}
}