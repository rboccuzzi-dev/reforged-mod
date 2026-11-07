package com.cuzzi.reforged.server;

import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class StatsService {

    private static final ResourceLocation STAT_HEALTH_ID = ResourceLocation.fromNamespaceAndPath("reforged", "stat_max_health");

    public static void refresh(ServerPlayer player) {
        refresh(player, false);
    }

    public static void refresh(ServerPlayer player, boolean isLogin) {
        PlayerStatsData data = ModAttachments.stats(player);

        float currentHealth = player.getHealth();
        float oldMaxHealth = player.getMaxHealth();

        // 1. Calculate health bonus from Endurance / Constitution stats
        int totalEndurance = data.baseValue(StatType.ENDURANCE) + data.bonus(StatType.ENDURANCE, player);
        double extraHealth = totalEndurance * 2.0; // 2 HP per point

        // 2. Apply dynamic MAX_HEALTH attribute modifier
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.removeModifier(STAT_HEALTH_ID);
            if (extraHealth > 0) {
                maxHealthAttr.addTransientModifier(new AttributeModifier(
                        STAT_HEALTH_ID, extraHealth, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        float newMaxHealth = player.getMaxHealth();

        // 3. Health Preservation Logic on Login / Stat Refresh
        if (isLogin) {
            if (data.savedHealth() > 0) {
                player.setHealth(Math.min(data.savedHealth(), newMaxHealth));
            } else {
                // Default to scaling ratio if no exact health was saved
                float ratio = oldMaxHealth > 0 ? (currentHealth / oldMaxHealth) : 1.0f;
                player.setHealth(Math.max(1.0f, newMaxHealth * ratio));
            }
        } else {
            // Adjust health proportionally if max health changed mid-game
            if (oldMaxHealth > 0 && oldMaxHealth != newMaxHealth) {
                float ratio = currentHealth / oldMaxHealth;
                player.setHealth(Math.clamp(newMaxHealth * ratio, 1.0f, newMaxHealth));
            }
        }

        // 4. Sync updated stats to client
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,
                new com.cuzzi.reforged.network.SyncStatsPayload(data));
    }

    /**
     * Handles XP gain when a player kills a mob.
     */
    public static void handleMobKill(ServerPlayer player, net.minecraft.world.entity.LivingEntity killedEntity) {
        if (killedEntity == null) return;
        int xpGain = Math.max(5, (int) (killedEntity.getMaxHealth() * 0.5));
        handleMobKill(player, xpGain);
    }

    /**
     * Directly awards XP to a player and refreshes their stats.
     */
    public static void handleMobKill(ServerPlayer player, int xpAmount) {
        PlayerStatsData data = ModAttachments.stats(player);
        data.addXp(xpAmount);
        refresh(player);
    }
}