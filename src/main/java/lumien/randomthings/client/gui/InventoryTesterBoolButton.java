package lumien.randomthings.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.BooleanSupplier;

public class InventoryTesterBoolButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/inventory_tester/invert_signal.png");
    private final BooleanSupplier valueSupplier;

    public InventoryTesterBoolButton(int x, int y, int width, int height, BooleanSupplier valueSupplier, OnPress onPress) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.valueSupplier = valueSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean value = valueSupplier.getAsBoolean();
        int textureX = value ? 20 : 0; // Right half for on, left half for off
        int textureY = 0; // Start with normal state
        
        // Determine button state (matches original 1.12.2 logic)
        int buttonState = 1; // Normal
        if (!this.active) {
            buttonState = 0; // Disabled  
        } else if (this.isHoveredOrFocused()) {
            buttonState = 2; // Hovered
        }
        
        // Calculate texture coordinates (matches original formula)
        textureX = value ? 20 : 0; // (value ? 1 : 0) * width
        textureY = (buttonState - 1) * 20; // (i - 1) * height
        
        // Draw the button texture from the 40x60 sprite sheet
        guiGraphics.blit(TEXTURE, this.getX(), this.getY(), textureX, textureY, this.width, this.height);
    }
}