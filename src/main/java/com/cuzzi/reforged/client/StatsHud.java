package com.cuzzi.reforged.client;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.List;

public class StatsHud {

    public static final LayeredDraw.Layer HUD_LAYER = (graphics, deltaTracker) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || !mc.gameMode.hasExperience()) return;

        Player player = mc.player;
        if (player == null || player.isSpectator()) return;

        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 81;
        int barHeight = 7;
        int leftX = (screenWidth / 2) - 91;
        int rightX = (screenWidth / 2) + 10;
        int baseY = screenHeight - 39;

        // --- 1. STYLIZED HEALTH BAR (Left side) ---
        float currentHp = player.getHealth();
        float maxHp = player.getMaxHealth();
        float hpRatio = Math.min(1.0f, Math.max(0.0f, currentHp / Math.max(1.0f, maxHp)));
        int filledHp = (int) (barWidth * hpRatio);

        // Frame & Background
        graphics.fill(leftX - 1, baseY - 1, leftX + barWidth + 1, baseY + barHeight + 1, 0xFF000000);
        graphics.fill(leftX, baseY, leftX + barWidth, baseY + barHeight, 0xFF1A0505);

        // Red Gradient Fill
        if (filledHp > 0) {
            graphics.fill(leftX, baseY, leftX + filledHp, baseY + barHeight, 0xFFE53935);
            graphics.fill(leftX, baseY, leftX + filledHp, baseY + 2, 0xFFFF6B6B);
        }

        graphics.drawString(mc.font, "❤", leftX - 10, baseY - 1, 0xFFFF4444, true);
        String hpText = (int) Math.ceil(currentHp) + " / " + (int) Math.ceil(maxHp);
        int hpTextWidth = mc.font.width(hpText);
        graphics.drawString(mc.font, hpText, leftX + (barWidth - hpTextWidth) / 2, baseY - 1, 0xFFFFFFFF, true);

        // --- 2. STYLIZED MANA & AURA BARS (Right side) ---
        List<StatType> visible = data.visibleStats();
        boolean hasMana = visible.contains(StatType.MANA);
        boolean hasAura = visible.contains(StatType.AURA);

        int currentY = baseY;

        // Mana Bar
        if (hasMana) {
            int currentMana = data.currentMana();
            int maxMana = data.maxMana(player);
            float manaRatio = Math.min(1.0f, Math.max(0.0f, (float) currentMana / Math.max(1, maxMana)));
            int filledMana = (int) (barWidth * manaRatio);

            graphics.fill(rightX - 1, currentY - 1, rightX + barWidth + 1, currentY + barHeight + 1, 0xFF000000);
            graphics.fill(rightX, currentY, rightX + barWidth, currentY + barHeight, 0xFF050A1A);

            if (filledMana > 0) {
                graphics.fill(rightX, currentY, rightX + filledMana, currentY + barHeight, 0xFF1E88E5);
                graphics.fill(rightX, currentY, rightX + filledMana, currentY + 2, 0xFF64B5F6);
            }

            graphics.drawString(mc.font, "✦", rightX + barWidth + 3, currentY - 1, 0xFF448AFF, true);
            String manaText = currentMana + " / " + maxMana;
            int manaTextWidth = mc.font.width(manaText);
            graphics.drawString(mc.font, manaText, rightX + (barWidth - manaTextWidth) / 2, currentY - 1, 0xFFFFFFFF, true);

            currentY -= 9;
        }

        // Aura Bar (rendered only if visible)
        if (hasAura) {
            int currentAura = data.currentAura();
            int maxAura = data.maxAura(player);
            float auraRatio = Math.min(1.0f, Math.max(0.0f, (float) currentAura / Math.max(1, maxAura)));
            int filledAura = (int) (barWidth * auraRatio);

            graphics.fill(rightX - 1, currentY - 1, rightX + barWidth + 1, currentY + barHeight + 1, 0xFF000000);
            graphics.fill(rightX, currentY, rightX + barWidth, currentY + barHeight, 0xFF051A18);

            if (filledAura > 0) {
                graphics.fill(rightX, currentY, rightX + filledAura, currentY + barHeight, 0xFF00ACC1);
                graphics.fill(rightX, currentY, rightX + filledAura, currentY + 2, 0xFF80DEEA);
            }

            graphics.drawString(mc.font, "🌀", rightX + barWidth + 3, currentY - 1, 0xFF00E5FF, true);
            String auraText = currentAura + " / " + maxAura;
            int auraTextWidth = mc.font.width(auraText);
            graphics.drawString(mc.font, auraText, rightX + (barWidth - auraTextWidth) / 2, currentY - 1, 0xFFFFFFFF, true);
        }

        // --- 3. FOOD BUFF INDICATOR ---
        if (data.foodBuffPercent() > 0) {
            int buffPercent = (int) (data.foodBuffPercent() * 100);
            int secondsLeft = data.foodBuffTicks() / 20;
            String buffText = "🍗 +" + buffPercent + "% (" + secondsLeft + "s)";
            graphics.drawString(mc.font, buffText, leftX, baseY - 10, 0xFFFFD700, true);
        }
    };

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.CROSSHAIR,
                ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "stats_hud"),
                HUD_LAYER
        );
    }
}