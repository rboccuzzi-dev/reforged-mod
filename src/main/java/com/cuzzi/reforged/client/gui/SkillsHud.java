package com.cuzzi.reforged.client.gui;

import com.cuzzi.reforged.Reforged;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.server.magic.spells.SpellRegistry;
import com.cuzzi.reforged.stats.PlayerStatsData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class SkillsHud {

    public static final LayeredDraw.Layer HUD_LAYER = (graphics, deltaTracker) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        PlayerStatsData data = ModAttachments.stats(player);
        if (data == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int slotSize = 22;
        int spacing = 4;
        int totalWidth = (slotSize * 5) + (spacing * 4);

        // Centrado arriba de la hotbar
        int startX = (screenWidth / 2) - (totalWidth / 2);
        int y = screenHeight - 65;

        for (int i = 0; i < 5; i++) {
            int x = startX + (i * (slotSize + spacing));

            // Fondo del Slot
            graphics.fill(x, y, x + slotSize, y + slotSize, 0x88000000);
            graphics.renderOutline(x, y, slotSize, slotSize, 0xFFAAAAAA);

            String skillId = data.skillSlots()[i];
            if (skillId != null && !skillId.isEmpty()) {
                Spell spell = SpellRegistry.get(skillId);
                if (spell != null) {
                    // Dibujar Ícono (Asumiendo que el ícono es 16x16 y el slot es 22x22)
                    graphics.blit(spell.getIcon(), x + 3, y + 3, 0, 0, 16, 16, 16, 16);

                    int cdTicks = data.getCooldown(skillId);
                    if (cdTicks > 0) {
                        // Oscurecer Ícono
                        graphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, 0xAA000000);

                        // Dibujar Timer (Segundos)
                        float seconds = cdTicks / 20.0f;
                        String timeText = seconds >= 10 ? String.valueOf((int)seconds) : String.format("%.1f", seconds);
                        int textW = mc.font.width(timeText);

                        graphics.pose().pushPose();
                        graphics.pose().translate(x + (slotSize / 2f) - (textW / 2f), y + 6, 100);
                        graphics.drawString(mc.font, timeText, 0, 0, 0xFFFFFF, true);
                        graphics.pose().popPose();
                    }
                }
            }

            // Dibujar número de atajo (1-5)
            graphics.pose().pushPose();
            graphics.pose().scale(0.6f, 0.6f, 1f);
            graphics.drawString(mc.font, String.valueOf(i + 1), (int)((x + 1) / 0.6f), (int)((y + 1) / 0.6f), 0xFFFFFF, true);
            graphics.pose().popPose();
        }
    };

    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(Reforged.MODID, "skills_hud"), HUD_LAYER);
    }
}