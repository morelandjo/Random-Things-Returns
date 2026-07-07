package lumien.randomthings.client.screen;

import lumien.randomthings.menu.IgniterMenu;
import lumien.randomthings.network.IgniterUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class IgniterScreen extends AbstractContainerScreen<IgniterMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/igniter.png");
    private Button modeButton;

    public IgniterScreen(IgniterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 78;
        this.imageHeight = 50;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addRenderableWidget(
            Button.builder(getModeButtonText(), button -> cycleMode())
                .bounds(this.leftPos + 7, this.topPos + 20, this.imageWidth - 14, 20).build());
    }

    private void cycleMode() {
        RTNetwork.sendToServer(new IgniterUpdatePacket(this.menu.getBlockEntity().getBlockPos()));
    }

    private Component getModeButtonText() {
        return Component.translatable(this.menu.getBlockEntity().getMode().getTranslationKey());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        // Keep the button label in sync with the synced BE mode.
        if (this.modeButton != null && !this.modeButton.getMessage().equals(getModeButtonText())) {
            this.modeButton.setMessage(getModeButtonText());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 4, 6, 4210752, false);
    }
}
