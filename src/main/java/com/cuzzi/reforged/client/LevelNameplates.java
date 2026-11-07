package com.cuzzi.reforged.client;

import com.cuzzi.reforged.registry.ModAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.joml.Matrix4f;

/**
 * "[Lv. N]" in front of the nameplate of players and mobs, and "health/max" under the name of mobs.
 * Registered from ClientInit (client only).
 */
public final class LevelNameplates {

    /** Mobs only show their nameplate within this distance and if you can see them. */
    private static final double MOB_MAX_DISTANCE = 20.0;

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        LocalPlayer self = Minecraft.getInstance().player;
        if (self == null) return;

        Entity entity = event.getEntity();

        if (entity instanceof Player other) {
            int level = other.getData(ModAttachments.PLAYER_LEVEL);
            if (level > 0) {
                event.setContent(prefix(level, ChatFormatting.GOLD).append(event.getContent()));
            }
            return;
        }

        if (entity instanceof Mob mob) {
            int level = mob.getData(ModAttachments.MOB_LEVEL);
            if (level <= 0) return;

            int myLevel = ModAttachments.stats(self).level();
            event.setContent(prefix(level, colorFor(level, myLevel)).append(event.getContent()));

            // Force the nameplate even if the mob has no custom name, but not through walls or from far away.
            if (mob.isAlive() && !mob.isInvisible()
                    && self.distanceToSqr(mob) <= MOB_MAX_DISTANCE * MOB_MAX_DISTANCE
                    && self.hasLineOfSight(mob)) {
                event.setCanRender(TriState.TRUE);
                renderHealthLine(event, mob);
            }
        }
    }

    /** Draws "health/max" one line under the name, the same way vanilla draws the nameplate. */
    private static void renderHealthLine(RenderNameTagEvent event, Mob mob) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 attachment = mob.getAttachments()
                .getNullable(EntityAttachment.NAME_TAG, 0, mob.getViewYRot(event.getPartialTick()));
        if (attachment == null) return;

        int health = (int) Math.ceil(mob.getHealth());
        int maxHealth = (int) Math.ceil(mob.getMaxHealth());
        int rgb = healthColor(maxHealth > 0 ? health / (float) maxHealth : 0);
        Component text = Component.literal(health + "/" + maxHealth);

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(attachment.x, attachment.y + 0.5, attachment.z);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();

        Font font = mc.font;
        float x = -font.width(text) / 2f;
        float y = 10; // one line below the name
        int background = (int) (mc.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;

        if (!mob.isDiscrete()) {
            // faint version visible through blocks, like vanilla
            font.drawInBatch(text, x, y, 0x20000000 | rgb, false, matrix, event.getMultiBufferSource(),
                    Font.DisplayMode.SEE_THROUGH, background, event.getPackedLight());
        }
        font.drawInBatch(text, x, y, 0xFF000000 | rgb, false, matrix, event.getMultiBufferSource(),
                Font.DisplayMode.NORMAL, 0, event.getPackedLight());
        pose.popPose();
    }

    private static int healthColor(float ratio) {
        if (ratio > 0.5f) return 0x55FF55;
        if (ratio > 0.25f) return 0xFFFF55;
        return 0xFF5555;
    }

    private static MutableComponent prefix(int level, ChatFormatting color) {
        return Component.literal("[Lv. " + level + "] ").withStyle(color);
    }

    /** Green = easy, yellow = even, gold = hard, red = much stronger than you. */
    private static ChatFormatting colorFor(int mobLevel, int playerLevel) {
        int diff = mobLevel - playerLevel;
        if (diff <= -3) return ChatFormatting.GREEN;
        if (diff <= 2) return ChatFormatting.YELLOW;
        if (diff <= 6) return ChatFormatting.GOLD;
        return ChatFormatting.RED;
    }

    private LevelNameplates() {}
}