package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.IronDropperBlockEntity;
import lumien.randomthings.menu.IronDropperMenu;
import lumien.randomthings.network.IronDropperUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The 1.21.1 version used a custom multi-state image-button widget for the four options; this downport
 * uses plain text buttons that show the current value. Behaviour is identical.
 */
public class IronDropperScreen extends AbstractContainerScreen<IronDropperMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/iron_dropper.png");

    private Button redstoneButton;
    private Button pickupButton;
    private Button motionButton;
    private Button effectsButton;

    public IronDropperScreen(IronDropperMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int bx = this.leftPos + 7;
        redstoneButton = addRenderableWidget(Button.builder(redstoneText(), b -> send(0)).bounds(bx, this.topPos + 16, 48, 16).build());
        pickupButton = addRenderableWidget(Button.builder(pickupText(), b -> send(1)).bounds(bx, this.topPos + 34, 48, 16).build());
        motionButton = addRenderableWidget(Button.builder(motionText(), b -> send(2)).bounds(this.leftPos + 121, this.topPos + 16, 48, 16).build());
        effectsButton = addRenderableWidget(Button.builder(effectsText(), b -> send(3)).bounds(this.leftPos + 121, this.topPos + 34, 48, 16).build());
    }

    private IronDropperBlockEntity be() {
        return this.menu.getBlockEntity();
    }

    private Component redstoneText() {
        return Component.literal("R: " + be().getRedstoneMode().name());
    }

    private Component pickupText() {
        return Component.literal("D: " + be().getPickupDelay().name());
    }

    private Component motionText() {
        return Component.literal("M: " + (be().isRandomMotion() ? "RAND" : "FIXED"));
    }

    private Component effectsText() {
        return Component.literal("FX: " + be().getEffects().name());
    }

    private void send(int action) {
        RTNetwork.sendToServer(new IronDropperUpdatePacket(be().getBlockPos(), action));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        // Keep labels in sync with the synced block-entity config.
        redstoneButton.setMessage(redstoneText());
        pickupButton.setMessage(pickupText());
        motionButton.setMessage(motionText());
        effectsButton.setMessage(effectsText());
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
