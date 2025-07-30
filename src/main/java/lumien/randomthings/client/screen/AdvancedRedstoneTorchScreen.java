package lumien.randomthings.client.screen;

import lumien.randomthings.menu.AdvancedRedstoneTorchMenu;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancedRedstoneTorchScreen extends AbstractContainerScreen<AdvancedRedstoneTorchMenu> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/advanced_redstone_torch.png");

    public AdvancedRedstoneTorchScreen(AdvancedRedstoneTorchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        
        this.imageWidth = 90;
        this.imageHeight = 56;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
            sendButtonPress(0);
        }).bounds(this.leftPos + 5, this.topPos + 15, 10, 10).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
            sendButtonPress(1);
        }).bounds(this.leftPos + 5 + 70, this.topPos + 15, 10, 10).build());

        this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
            sendButtonPress(2);
        }).bounds(this.leftPos + 5, this.topPos + 39, 10, 10).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
            sendButtonPress(3);
        }).bounds(this.leftPos + 5 + 70, this.topPos + 39, 10, 10).build());
    }

    private void sendButtonPress(int buttonId) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buffer.writeInt(buttonId);
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        PacketDistributor.sendToServer(new ContainerSignalMessage(0, data));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_torch.gs"), 8, 5, 0x000000, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_torch.rs"), 13, 29, 0x000000, false);

        if (this.menu instanceof AdvancedRedstoneTorchMenu artMenu) {
            String signalStrengthGreenString = artMenu.getSignalStrengthGreen() + "";
            int greenStringWidth = this.font.width(signalStrengthGreenString);
            guiGraphics.drawString(this.font, signalStrengthGreenString, 
                imageWidth / 2 - greenStringWidth / 2, 16, DyeColor.GREEN.getFireworkColor(), false);

            String signalStrengthRedString = artMenu.getSignalStrengthRed() + "";
            int redStringWidth = this.font.width(signalStrengthRedString);
            guiGraphics.drawString(this.font, signalStrengthRedString, 
                imageWidth / 2 - redStringWidth / 2, 40, DyeColor.RED.getFireworkColor(), false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}