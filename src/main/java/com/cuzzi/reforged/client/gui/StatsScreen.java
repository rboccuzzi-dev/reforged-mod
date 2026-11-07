package com.cuzzi.reforged.client.gui;

import com.cuzzi.reforged.network.ApplyStatsPayload;
import com.cuzzi.reforged.network.ServerboundSelectTitlePayload;
import com.cuzzi.reforged.registry.ModAttachments;
import com.cuzzi.reforged.server.magic.spells.Spell;
import com.cuzzi.reforged.stats.ClassDefinition;
import com.cuzzi.reforged.stats.PlayerStatsData;
import com.cuzzi.reforged.stats.StatType;
import com.cuzzi.reforged.titles.TitleDefinition;
import com.cuzzi.reforged.titles.TitleRegistry;
import com.cuzzi.reforged.util.AuxFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsScreen extends Screen {

    private final Map<StatType, Integer> pendingAllocations = new HashMap<>();
    private final List<StatRowData> rowDataList = new ArrayList<>();
    private final List<TitleDefinition> unlockedTitlesSorted = new ArrayList<>();

    private static final int GUI_WIDTH = 270;
    private static final int GUI_HEIGHT = 220;
    private int leftPos;
    private int topPos;

    private int activeTab = 0;
    private double scrollAmount = 0;

    private static final int ROW_HEIGHT = 20;
    private static final int TITLE_ROW_HEIGHT = 32;
    private static final int LIST_X_OFFSET = 92;
    private static final int LIST_Y_OFFSET = 45;
    private static final int LIST_WIDTH = 170;
    private static final int LIST_HEIGHT = 130;

    private static final int TITLE_BTN_Y_OFFSET = 8;
    private static final int TITLE_BTN_WIDTH = 20;
    private static final int TITLE_BTN_HEIGHT = 14;
    private static final int DISPLAY_BTN_X_OFFSET = 114;
    private static final int ACTIVE_BTN_X_OFFSET = 137;

    private Button applyButton;

    public StatsScreen() {
        super(Component.literal("Character Profile"));
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;

        this.clearWidgets();
        this.rowDataList.clear();
        this.unlockedTitlesSorted.clear();

        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return;

        PlayerStatsData data = ModAttachments.stats(player);

        for (StatType type : data.visibleStats()) {
            rowDataList.add(new StatRowData(type));
        }

        for (String titleId : data.unlockedTitles()) {
            TitleDefinition def = TitleRegistry.get(titleId);
            if (def != null) {
                unlockedTitlesSorted.add(def);
            }
        }
        unlockedTitlesSorted.sort((a, b) -> Integer.compare(
                AuxFunctions.getRarityPriority(b.rarity()),
                AuxFunctions.getRarityPriority(a.rarity())
        ));

        // Pestañas
        this.addRenderableWidget(Button.builder(Component.literal("Stats"), b -> switchTab(0))
                .bounds(leftPos + 6, topPos - 18, 44, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("Skills"), b -> switchTab(1))
                .bounds(leftPos + 52, topPos - 18, 44, 18).build());

        this.addRenderableWidget(Button.builder(Component.literal("Titles"), b -> switchTab(2))
                .bounds(leftPos + 98, topPos - 18, 44, 18).build());

        Button questsBtn = Button.builder(Component.literal("Quests"), b -> switchTab(3))
                .bounds(leftPos + 144, topPos - 18, 48, 18).build();
        questsBtn.active = false;
        this.addRenderableWidget(questsBtn);

        // Botón Aplicar
        this.applyButton = Button.builder(Component.literal("Apply"), b -> applyChanges())
                .bounds(leftPos + 165, topPos + 182, 95, 20).build();
        this.applyButton.visible = (activeTab == 0);
        this.applyButton.active = false;
        this.addRenderableWidget(this.applyButton);

        updateButtonStates();
    }

    private void switchTab(int tabIndex) {
        this.activeTab = tabIndex;
        this.scrollAmount = 0;
        if (this.applyButton != null) {
            this.applyButton.visible = (activeTab == 0);
        }
    }

    private void adjustPending(StatType type, int delta) {
        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return;

        PlayerStatsData data = ModAttachments.stats(player);
        int currentPending = pendingAllocations.getOrDefault(type, 0);
        int totalPendingAll = pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();

        if (delta > 0 && totalPendingAll < data.remainingPoints()) {
            pendingAllocations.put(type, currentPending + 1);
        } else if (delta < 0 && currentPending > 0) {
            int updated = currentPending - 1;
            if (updated <= 0) {
                pendingAllocations.remove(type);
            } else {
                pendingAllocations.put(type, updated);
            }
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        if (this.applyButton != null) {
            this.applyButton.active = !pendingAllocations.isEmpty();
        }
    }

    private void applyChanges() {
        if (!pendingAllocations.isEmpty()) {
            Map<String, Integer> stringAllocations = new HashMap<>();
            pendingAllocations.forEach((type, amount) -> {
                if (amount > 0) stringAllocations.put(type.id(), amount);
            });

            PacketDistributor.sendToServer(new ApplyStatsPayload(stringAllocations));
            pendingAllocations.clear();
            updateButtonStates();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int contentHeight = (activeTab == 2) ? unlockedTitlesSorted.size() * TITLE_ROW_HEIGHT : rowDataList.size() * ROW_HEIGHT;
        int maxScroll = Math.max(0, contentHeight - LIST_HEIGHT);

        if (maxScroll > 0) {
            this.scrollAmount = Mth.clamp(this.scrollAmount - (scrollY * 12), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listX = leftPos + LIST_X_OFFSET;
        int listY = topPos + LIST_Y_OFFSET;

        if (mouseX >= listX && mouseX <= listX + LIST_WIDTH && mouseY >= listY && mouseY <= listY + LIST_HEIGHT) {
            Player player = this.minecraft != null ? this.minecraft.player : null;
            if (player != null) {
                PlayerStatsData data = ModAttachments.stats(player);

                if (activeTab == 0) {
                    int totalPendingAll = pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();
                    int startY = listY - (int) scrollAmount;

                    for (int i = 0; i < rowDataList.size(); i++) {
                        StatType type = rowDataList.get(i).type;
                        int rowY = startY + (i * ROW_HEIGHT);

                        if (rowY + ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) continue;

                        if (type.isAllocatable()) {
                            int minusX = listX + LIST_WIDTH - 36;
                            int plusX = listX + LIST_WIDTH - 18;

                            if (mouseY >= rowY + 2 && mouseY <= rowY + 16) {
                                if (mouseX >= minusX && mouseX <= minusX + 14 && pendingAllocations.getOrDefault(type, 0) > 0) {
                                    adjustPending(type, -1);
                                    return true;
                                } else if (mouseX >= plusX && mouseX <= plusX + 14 && totalPendingAll < data.remainingPoints()) {
                                    adjustPending(type, 1);
                                    return true;
                                }
                            }
                        }
                    }
                } else if (activeTab == 2) {
                    int startY = listY - (int) scrollAmount;

                    for (int i = 0; i < unlockedTitlesSorted.size(); i++) {
                        TitleDefinition title = unlockedTitlesSorted.get(i);
                        int rowY = startY + (i * TITLE_ROW_HEIGHT);

                        if (rowY + TITLE_ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) continue;

                        int displayBtnX = listX + DISPLAY_BTN_X_OFFSET;
                        int activeBtnX = listX + ACTIVE_BTN_X_OFFSET;
                        int btnY = rowY + TITLE_BTN_Y_OFFSET;

                        if (mouseY >= btnY && mouseY <= btnY + TITLE_BTN_HEIGHT) {
                            if (mouseX >= displayBtnX && mouseX <= displayBtnX + TITLE_BTN_WIDTH) {
                                boolean isCurrentlyDisplay = title.id().equals(data.displayTitle());
                                PacketDistributor.sendToServer(new ServerboundSelectTitlePayload(isCurrentlyDisplay ? "" : title.id(), true));
                                return true;
                            } else if (mouseX >= activeBtnX && mouseX <= activeBtnX + TITLE_BTN_WIDTH) {
                                boolean isCurrentlyActive = title.id().equals(data.activeTitle());
                                PacketDistributor.sendToServer(new ServerboundSelectTitlePayload(isCurrentlyActive ? "" : title.id(), false));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x80000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        Player player = this.minecraft != null ? this.minecraft.player : null;
        if (player == null) return;

        PlayerStatsData data = ModAttachments.stats(player);
        ClassDefinition currentClass = data.playerClass();

        AuxFunctions.drawPanel(graphics, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT, 0xFF1C92D2, 0xFF142230);

        int boxX1 = leftPos + 8;
        int boxY1 = topPos + 10;
        int boxX2 = leftPos + 85;
        int boxY2 = topPos + 175;
        AuxFunctions.drawPanel(graphics, boxX1, boxY1, 77, 165, 0xFF1C92D2, 0xFF0A1118);

        InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics, boxX1, boxY1, boxX2, boxY2,
                45, 0.0625f, (float) mouseX, (float) mouseY, player
        );

        super.render(graphics, mouseX, mouseY, partialTick);

        int headerX = leftPos + LIST_X_OFFSET;
        int headerY = topPos + 10;

        TitleDefinition displayTitle = data.displayTitleDefinition();
        if (displayTitle != null) {
            Component titleComp = Component.literal("[" + displayTitle.displayName().getString() + "]")
                    .withStyle(displayTitle.rarity().color());
            graphics.drawString(this.font, titleComp, headerX, headerY, 0xFFFFFF);
        } else {
            graphics.drawString(this.font, Component.literal("[No Title]").withStyle(ChatFormatting.GRAY), headerX, headerY, 0xFFFFFF);
        }

        graphics.drawString(this.font, player.getName().getString(), headerX, headerY + 11, 0xFFFFFF);
        graphics.drawString(this.font, Component.literal(AuxFunctions.capitalize(currentClass.id())).withStyle(currentClass.rarity().color()), headerX, headerY + 22, 0xFFFFFF);

        if (activeTab == 0) {
            int remainingPoints = data.remainingPoints() - pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();
            graphics.drawString(this.font, Component.literal("Points: " + remainingPoints).withStyle(ChatFormatting.YELLOW), leftPos + 195, headerY + 11, 0xFFFFFF);
        }

        int listX = leftPos + LIST_X_OFFSET;
        int listY = topPos + LIST_Y_OFFSET;

        AuxFunctions.drawPanel(graphics, listX - 2, listY - 2, LIST_WIDTH + 4, LIST_HEIGHT + 4, 0xFF19547B, 0xFF0D1620);

        graphics.enableScissor(listX, listY, listX + LIST_WIDTH, listY + LIST_HEIGHT);
        int startY = listY - (int) scrollAmount;

        TitleDefinition hoveredTitle = null;

        if (activeTab == 0) {
            int totalPendingAll = pendingAllocations.values().stream().mapToInt(Integer::intValue).sum();
            int textX = listX + 6;

            for (int i = 0; i < rowDataList.size(); i++) {
                StatType type = rowDataList.get(i).type;
                int rowY = startY + (i * ROW_HEIGHT);

                if (rowY + ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) continue;

                String statName = AuxFunctions.formatStatName(type);
                String valueText;
                int mainColor = 0xE0E0E0;

                int itemBonus = data.itemBonus(type, player);
                int titleBonus = data.titleBonus(type, player);

                if (type == StatType.HEALTH) {
                    valueText = statName + ": " + (int) Math.ceil(player.getHealth()) + "/" + (int) player.getMaxHealth();
                } else if (type == StatType.MANA) {
                    valueText = statName + ": " + data.current(type, player) + "/" + (100 + titleBonus + itemBonus);
                    mainColor = 0x5555FF;
                } else if (type == StatType.AURA) {
                    valueText = statName + ": " + data.current(type, player) + "/" + (100 + titleBonus + itemBonus);
                    mainColor = 0x55FFFF;
                } else if (type == StatType.DEMONIC_POWER) {
                    valueText = statName + ": " + data.current(type, player);
                    mainColor = 0xAA00AA;
                } else {
                    int base = data.baseValue(type);
                    int pending = pendingAllocations.getOrDefault(type, 0);
                    int effectiveTotal = base + pending + itemBonus + titleBonus;

                    valueText = statName + ": " + effectiveTotal + "/" + (base + pending);
                    if (pending > 0) mainColor = 0xFFFF55;
                }

                graphics.drawString(this.font, valueText, textX, rowY + 4, mainColor);

                int currentOffsetX = textX + this.font.width(valueText) + 3;
                if (itemBonus > 0) {
                    String itemText = "(+" + itemBonus + ")";
                    graphics.drawString(this.font, itemText, currentOffsetX, rowY + 4, 0x55FF55);
                    currentOffsetX += this.font.width(itemText) + 2;
                }
                if (titleBonus > 0) {
                    String titleText = "(+" + titleBonus + ")";
                    graphics.drawString(this.font, titleText, currentOffsetX, rowY + 4, 0xFFB3DA);
                }

                if (type.isAllocatable()) {
                    int minusX = listX + LIST_WIDTH - 36;
                    int plusX = listX + LIST_WIDTH - 18;
                    int btnY = rowY + 2;

                    int pending = pendingAllocations.getOrDefault(type, 0);
                    boolean canMinus = pending > 0;
                    boolean canPlus = totalPendingAll < data.remainingPoints();

                    AuxFunctions.drawPanel(graphics, minusX, btnY, 14, 14, canMinus ? 0xFF55AABB : 0xFF2A3B4C, canMinus ? 0xFF334455 : 0xFF1C2834);
                    graphics.drawString(this.font, "-", minusX + 4, btnY + 3, canMinus ? 0xFFFFFF : 0x777777);

                    AuxFunctions.drawPanel(graphics, plusX, btnY, 14, 14, canPlus ? 0xFF55AABB : 0xFF2A3B4C, canPlus ? 0xFF334455 : 0xFF1C2834);
                    graphics.drawString(this.font, "+", plusX + 4, btnY + 3, canPlus ? 0xFFFFFF : 0x777777);
                }
            }
        } else if (activeTab == 1) {
            // --- PESTAÑA DE SKILLS ---
            if (data.unlockedSkills().isEmpty()) {
                graphics.drawString(this.font, Component.literal("No skills unlocked.").withStyle(ChatFormatting.GRAY), listX + 10, listY + 50, 0xFFFFFF);
            } else {
                List<String> skills = new ArrayList<>(data.unlockedSkills());
                for (int i = 0; i < skills.size(); i++) {
                    int rowY = startY + (i * TITLE_ROW_HEIGHT);
                    if (rowY + TITLE_ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) continue;

                    Spell spell = com.cuzzi.reforged.server.magic.spells.SpellRegistry.get(skills.get(i));
                    if (spell == null) continue;

                    int cardX = listX + 2;
                    int cardW = LIST_WIDTH - 12;
                    int cardY = rowY + 1;
                    int cardH = TITLE_ROW_HEIGHT - 3;

                    // Detectar Hover para Tooltip
                    if (mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(spell.getName().copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                        tooltip.add(spell.getDescription().copy().withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.empty());

                        ChatFormatting costColor = spell.getResourceType() == StatType.MANA ? ChatFormatting.BLUE :
                                (spell.getResourceType() == StatType.AURA ? ChatFormatting.AQUA : ChatFormatting.DARK_PURPLE);

                        tooltip.add(Component.literal("Cost: " + spell.getCost() + " " + spell.getResourceType().name()).withStyle(costColor));
                        tooltip.add(Component.literal("Cooldown: " + (spell.getCooldownTicks() / 20.0f) + "s").withStyle(ChatFormatting.YELLOW));

                        graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                    }

                    // Fondo de la skill
                    AuxFunctions.drawPanel(graphics, cardX, cardY, cardW, cardH, 0xFF9932CC, 0xFF121E2B);

                    // Icono de la Skill
                    graphics.blit(spell.getIcon(), cardX + 4, cardY + 4, 0, 0, 20, 20, 20, 20);

                    // Nombre
                    graphics.drawString(this.font, spell.getName(), cardX + 30, cardY + 4, 0xFFFFFF);

                    // Botones de Slot [1] [2] [3] [4] [5]
                    for (int s = 0; s < 5; s++) {
                        int btnX = cardX + 30 + (s * 18);
                        int btnY = cardY + 15;
                        boolean isActiveSlot = spell.getId().equals(data.skillSlots()[s]);

                        int outlineColor = isActiveSlot ? 0xFF00FF00 : 0xFF446688;
                        int fillColor = isActiveSlot ? 0xFF007700 : 0xFF223344;

                        AuxFunctions.drawPanel(graphics, btnX, btnY, 14, 12, outlineColor, fillColor);
                        graphics.drawString(this.font, String.valueOf(s + 1), btnX + 4, btnY + 2, isActiveSlot ? 0xFFFFFF : 0xAAAAAA);

                        // Lógica de click (En el mouseClicked)
                        // Si mouseClicked -> PacketDistributor.sendToServer(new ServerboundAssignSkillPayload(s, spell.getId()));
                    }
                }
            }

        } else if (activeTab == 2) {
            if (unlockedTitlesSorted.isEmpty()) {
                graphics.drawString(this.font, Component.literal("No titles unlocked.").withStyle(ChatFormatting.GRAY), listX + 10, listY + 50, 0xFFFFFF);
            } else {
                for (int i = 0; i < unlockedTitlesSorted.size(); i++) {
                    TitleDefinition title = unlockedTitlesSorted.get(i);
                    int rowY = startY + (i * TITLE_ROW_HEIGHT);

                    if (rowY + TITLE_ROW_HEIGHT < listY || rowY > listY + LIST_HEIGHT) continue;

                    boolean isDisplay = title.id().equals(data.displayTitle());
                    boolean isActive = title.id().equals(data.activeTitle());

                    int cardX = listX + 2;
                    int cardW = LIST_WIDTH - 12;
                    int cardY = rowY + 1;
                    int cardH = TITLE_ROW_HEIGHT - 3;

                    if (mouseX >= cardX && mouseX <= cardX + cardW && mouseY >= cardY && mouseY <= cardY + cardH) {
                        hoveredTitle = title;
                    }

                    AuxFunctions.drawPanel(graphics, cardX, cardY, cardW, cardH, isDisplay ? 0xFFFFD700 : 0xFF1C92D2, 0xFF121E2B);

                    int maxTitleWidth = DISPLAY_BTN_X_OFFSET - 10;
                    Component rawTitleComp = title.displayName().copy().withStyle(title.rarity().color());
                    String titleString = rawTitleComp.getString();
                    String trimmedString = this.font.plainSubstrByWidth(titleString, maxTitleWidth);
                    if (!trimmedString.equals(titleString)) {
                        trimmedString = this.font.plainSubstrByWidth(titleString, maxTitleWidth - 6) + "...";
                    }

                    graphics.drawString(this.font, Component.literal(trimmedString).withStyle(title.rarity().color()), listX + 6, rowY + 4, 0xFFFFFF);
                    graphics.drawString(this.font, Component.literal("[" + title.rarity().name() + "]").withStyle(ChatFormatting.DARK_GRAY), listX + 6, rowY + 16, 0x888888);

                    int displayBtnX = listX + DISPLAY_BTN_X_OFFSET;
                    int activeBtnX = listX + ACTIVE_BTN_X_OFFSET;
                    int btnY = rowY + TITLE_BTN_Y_OFFSET;

                    // Display [D]
                    AuxFunctions.drawPanel(graphics, displayBtnX, btnY, TITLE_BTN_WIDTH, TITLE_BTN_HEIGHT, isDisplay ? 0xFFFFD700 : 0xFF446688, isDisplay ? 0xFF997700 : 0xFF223344);
                    graphics.drawString(this.font, "D", displayBtnX + 7, btnY + 3, isDisplay ? 0xFFFFFF : 0xAAAAAA);

                    // Active [A]
                    AuxFunctions.drawPanel(graphics, activeBtnX, btnY, TITLE_BTN_WIDTH, TITLE_BTN_HEIGHT, isActive ? 0xFF00FF88 : 0xFF446688, isActive ? 0xFF007744 : 0xFF223344);
                    graphics.drawString(this.font, "A", activeBtnX + 7, btnY + 3, isActive ? 0xFFFFFF : 0xAAAAAA);
                }
            }
        }

        graphics.disableScissor();

        if (hoveredTitle != null) {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(hoveredTitle.displayName().copy().withStyle(hoveredTitle.rarity().color()).withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.literal("Rarity: " + hoveredTitle.rarity().name()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.empty());

            tooltip.add(Component.literal("--- Passive Bonuses (Always On) ---").withStyle(ChatFormatting.GOLD));
            if (!hoveredTitle.inactiveBonuses().isEmpty() || hoveredTitle.inactiveAllStatsBonus() > 0) {
                if (hoveredTitle.inactiveAllStatsBonus() > 0) {
                    tooltip.add(Component.literal("  +" + hoveredTitle.inactiveAllStatsBonus() + " to All Stats").withStyle(ChatFormatting.GREEN));
                }
                for (Map.Entry<StatType, Integer> e : hoveredTitle.inactiveBonuses().entrySet()) {
                    tooltip.add(Component.literal("  +" + e.getValue() + " " + AuxFunctions.formatStatName(e.getKey())).withStyle(ChatFormatting.GREEN));
                }
            } else {
                tooltip.add(Component.literal("  None").withStyle(ChatFormatting.DARK_GRAY));
            }

            tooltip.add(Component.literal("--- Active Bonus (When Equipped) ---").withStyle(ChatFormatting.AQUA));
            if (!hoveredTitle.activeBonuses().isEmpty() || hoveredTitle.activeAllStatsBonus() > 0) {
                if (hoveredTitle.activeAllStatsBonus() > 0) {
                    tooltip.add(Component.literal("  +" + hoveredTitle.activeAllStatsBonus() + " to All Stats").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                for (Map.Entry<StatType, Integer> e : hoveredTitle.activeBonuses().entrySet()) {
                    tooltip.add(Component.literal("  +" + e.getValue() + " " + AuxFunctions.formatStatName(e.getKey())).withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            } else {
                tooltip.add(Component.literal("  None").withStyle(ChatFormatting.DARK_GRAY));
            }

            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        int footerY = topPos + 182;
        graphics.drawString(this.font, "Level " + data.level(), leftPos + 10, footerY, 0xFFD966);
        graphics.drawString(this.font, "XP: " + data.xp() + " / " + data.xpToNext(), leftPos + 10, footerY + 11, 0xAAAAAA);
    }

    private record StatRowData(StatType type) {}
}