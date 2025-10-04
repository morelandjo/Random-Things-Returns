package lumien.randomthings.client.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A toggle button that renders with custom sprite textures
 * Supports two states (on/off) with different tooltips for each
 * The button texture layout is: [off][on] horizontally, [normal][hover][disabled] vertically
 */
public class ToggleButton extends AbstractButton {
    private final ResourceLocation texture;
    private final int textureX;
    private final int textureY;
    private boolean state;
    private final Component tooltipOff;
    private final Component tooltipOn;
    private final OnPress onPress;

    public ToggleButton(int x, int y, int width, int height,
                        ResourceLocation texture, int textureX, int textureY,
                        boolean initialState,
                        Component tooltipOff, Component tooltipOn,
                        OnPress onPress) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.textureX = textureX;
        this.textureY = textureY;
        this.state = initialState;
        this.tooltipOff = tooltipOff;
        this.tooltipOn = tooltipOn;
        this.onPress = onPress;
    }

    @Override
    public void onPress() {
        // Toggle state immediately for visual feedback
        this.state = !this.state;
        // Then notify the callback
        this.onPress.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Calculate UV offset based on state and hover
        // Horizontal: 0 = off, 20 = on
        int uOffset = state ? 20 : 0;

        // Vertical: 0 = normal, 20 = hover, 40 = disabled
        int vOffset = 0;
        if (!active) {
            vOffset = 40;
        } else if (isHovered) {
            vOffset = 20;
        }

        // Draw the button sprite
        guiGraphics.blit(texture,
            getX(), getY(),
            textureX + uOffset, textureY + vOffset,
            width, height,
            256, 256);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public Component getTooltipComponent() {
        return state ? tooltipOn : tooltipOff;
    }

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @FunctionalInterface
    public interface OnPress {
        void onPress(ToggleButton button);
    }
}
