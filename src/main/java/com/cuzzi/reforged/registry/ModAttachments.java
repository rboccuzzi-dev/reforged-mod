package com.cuzzi.reforged.registry;

import com.cuzzi.reforged.stats.PlayerStatsData;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "reforged");

    /** Stats of a player. Saved with the player and kept after death. */
    public static final Supplier<AttachmentType<PlayerStatsData>> PLAYER_STATS =
            ATTACHMENTS.register("player_stats", () ->
                    AttachmentType.builder(PlayerStatsData::new)
                            .serialize(PlayerStatsData.CODEC)
                            .copyOnDeath()
                            .build());

    /** Level of a mob. 0 = not assigned yet. Saved with the mob and synced to clients (for the nameplate). */
    public static final Supplier<AttachmentType<Integer>> MOB_LEVEL =
            ATTACHMENTS.register("mob_level", () ->
                    AttachmentType.builder(() -> 0)
                            .serialize(Codec.INT)
                            .sync(ByteBufCodecs.VAR_INT)
                            .build());

    /**
     * Level of a player, synced to everyone who can see them (for the nameplate).
     * Not saved: StatsService.refresh() recomputes it from PLAYER_STATS.
     */
    public static final Supplier<AttachmentType<Integer>> PLAYER_LEVEL =
            ATTACHMENTS.register("player_level", () ->
                    AttachmentType.builder(() -> 0)
                            .sync(ByteBufCodecs.VAR_INT)
                            .build());

    public static PlayerStatsData stats(Player player) {
        return player.getData(PLAYER_STATS);
    }

    private ModAttachments() {}
}