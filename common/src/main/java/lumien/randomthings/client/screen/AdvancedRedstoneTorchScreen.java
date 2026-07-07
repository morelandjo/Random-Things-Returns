package lumien.randomthings.client.screen;

import lumien.randomthings.menu.AdvancedRedstoneTorchMenu;
import lumien.randomthings.network.AdvancedRedstoneTorchUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

public class AdvancedRedstoneTorchScreen extends AbstractContainerScreen<AdvancedRedstoneTorchMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/advanced_redstone_torch.png");

    public AdvancedRedstoneTorchScreen(AdvancedRedstoneTorchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 90;
        this.imageHeight = 56;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> send(0))
            .bounds(this.leftPos + 5, this.topPos + 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> send(1))
            .bounds(this.leftPos + this.imageWidth - 25, this.topPos + 10, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> send(2))
            .bounds(this.leftPos + 5, this.topPos + 34, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> send(3))
            .bounds(this.leftPos + this.imageWidth - 25, this.topPos + 34, 20, 20).build());
    }

    private void send(int action) {
        RTNetwork.sendToServer(new AdvancedRedstoneTorchUpdatePacket(this.menu.getBlockEntity().getBlockPos(), action));
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
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        String green = String.valueOf(this.menu.getSignalStrengthGreen());
        guiGraphics.drawString(this.font, green, this.imageWidth / 2 - this.font.width(green) / 2, 16, DyeColor.GREEN.getFireworkColor(), false);
        String red = String.valueOf(this.menu.getSignalStrengthRed());
        guiGraphics.drawString(this.font, red, this.imageWidth / 2 - this.font.width(red) / 2, 40, DyeColor.RED.getFireworkColor(), false);
    }
}
