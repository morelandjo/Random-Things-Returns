package lumien.randomthings.client.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A two-state toggle button rendered from a sprite sheet. Texture layout: [off][on] horizontally,
 * [normal][hover][disabled] vertically.
 */
@Environment(EnvType.CLIENT)
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
        this.state = !this.state;
        this.onPress.onPress(this);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int uOffset = state ? 20 : 0;
        int vOffset = 0;
        if (!active) {
            vOffset = 40;
        } else if (isHovered) {
            vOffset = 20;
        }
        guiGraphics.blit(texture, getX(), getY(), textureX + uOffset, textureY + vOffset, width, height, 256, 256);
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
