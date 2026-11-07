package com.cuzzi.reforged.client.gui;

import com.cuzzi.reforged.network.ServerboundSubmitLegendaryNamePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class LegendaryNamingScreen extends Screen {

    private EditBox nameInput;
    private Button forgeButton;

    // GUI Dimensions
    private final int xSize = 200;
    private final int ySize = 120;
    private int guiLeft;
    private int guiTop;

    public LegendaryNamingScreen() {
        super(Component.literal("Name Your Legendary Artifact"));
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        // Text Input Box
        this.nameInput = new EditBox(this.font, this.guiLeft + 20, this.guiTop + 50, 160, 20, Component.literal("Artifact Name"));
        this.nameInput.setMaxLength(35); // Prevent names that are too long
        this.nameInput.setResponder(text -> updateButtonState()); // Update button when typing
        this.addRenderableWidget(this.nameInput);
        this.setInitialFocus(this.nameInput); // Auto-focus so they can start typing immediately

        // Submit Button
        this.forgeButton = Button.builder(Component.literal("Forge Artifact"), button -> submitName())
                .bounds(this.guiLeft + 50, this.guiTop + 85, 100, 20)
                .build();
        this.forgeButton.active = false; // Disabled until they type something
        this.addRenderableWidget(this.forgeButton);
    }

    private void updateButtonState() {
        // Only allow submitting if the name is not empty or just spaces
        this.forgeButton.active = !this.nameInput.getValue().trim().isEmpty();
    }

    private void submitName() {
        String customName = this.nameInput.getValue().trim();
        if (!customName.isEmpty()) {
            // Send the network packet to the server
            PacketDistributor.sendToServer(new ServerboundSubmitLegendaryNamePayload(customName));
            // Close the screen
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If they press Enter, submit the name
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.forgeButton.active) {
                submitName();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Darken the world behind the GUI
        graphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Draw the main background matching your blue UI style
        // Outer border (Light Blue)
        graphics.fill(guiLeft - 2, guiTop - 2, guiLeft + xSize + 2, guiTop + ySize + 2, 0xFF1C92D2);
        // Inner background (Darker Blue)
        graphics.fill(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF19547B);

        // Inner panel for the text area (Matching your panel style)
        graphics.fill(guiLeft + 10, guiTop + 40, guiLeft + xSize - 10, guiTop + 110, 0xFF123B59);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Draw the background rectangles
        super.renderBackground(graphics, mouseX, mouseY, partialTick);

        // Draw the title text (Gold, centered)
        graphics.drawCenteredString(this.font, Component.literal("A LEGENDARY ARTIFACT IS BORN").withStyle(net.minecraft.ChatFormatting.GOLD), this.width / 2, this.guiTop + 15, 0xFFFFFF);

        // Subtitle
        graphics.drawCenteredString(this.font, Component.literal("Bestow a name upon your creation:"), this.width / 2, this.guiTop + 30, 0xAAAAAA);

        // Draw the text box and button
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game in multiplayer
    }
}