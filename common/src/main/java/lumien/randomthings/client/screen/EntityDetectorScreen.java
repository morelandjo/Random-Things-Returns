package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.EntityDetectorBlockEntity;
import lumien.randomthings.blockentity.EntityDetectorBlockEntity.Filter;
import lumien.randomthings.menu.EntityDetectorMenu;
import lumien.randomthings.network.EntityDetectorUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class EntityDetectorScreen extends AbstractContainerScreen<EntityDetectorMenu> {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("randomthings", "textures/gui/entity_detector.png");

    private Button filterButton;
    private Button invertButton;
    private Button strongOutputButton;

    public EntityDetectorScreen(EntityDetectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        this.filterButton = this.addRenderableWidget(
            Button.builder(filterLabel(), btn -> send(EntityDetectorUpdatePacket.ACTION_CYCLE_FILTER, 0))
                .bounds(x + 8, y + 18, 90, 18).build());

        this.addRenderableWidget(
            Button.builder(Component.literal("-"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_X, this.menu.getBlockEntity().getRangeX() - 1))
                .bounds(x + 8, y + 40, 16, 16).build());
        this.addRenderableWidget(
            Button.builder(Component.literal("+"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_X, this.menu.getBlockEntity().getRangeX() + 1))
                .bounds(x + 82, y + 40, 16, 16).build());

        this.addRenderableWidget(
            Button.builder(Component.literal("-"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_Y, this.menu.getBlockEntity().getRangeY() - 1))
                .bounds(x + 8, y + 56, 16, 16).build());
        this.addRenderableWidget(
            Button.builder(Component.literal("+"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_Y, this.menu.getBlockEntity().getRangeY() + 1))
                .bounds(x + 82, y + 56, 16, 16).build());

        this.addRenderableWidget(
            Button.builder(Component.literal("-"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_Z, this.menu.getBlockEntity().getRangeZ() - 1))
                .bounds(x + 8, y + 72, 16, 16).build());
        this.addRenderableWidget(
            Button.builder(Component.literal("+"), btn -> send(EntityDetectorUpdatePacket.ACTION_SET_RANGE_Z, this.menu.getBlockEntity().getRangeZ() + 1))
                .bounds(x + 82, y + 72, 16, 16).build());

        this.invertButton = this.addRenderableWidget(
            Button.builder(invertLabel(), btn -> send(EntityDetectorUpdatePacket.ACTION_TOGGLE_INVERT, 0))
                .bounds(x + 102, y + 18, 66, 18).build());

        this.strongOutputButton = this.addRenderableWidget(
            Button.builder(strongLabel(), btn -> send(EntityDetectorUpdatePacket.ACTION_TOGGLE_STRONG, 0))
                .bounds(x + 102, y + 38, 46, 18).build());
    }

    private void send(int action, int value) {
        RTNetwork.sendToServer(new EntityDetectorUpdatePacket(
            this.menu.getBlockEntity().getBlockPos(), action, value));
    }

    private Component filterLabel() {
        Filter f = this.menu.getBlockEntity().getFilter();
        return Component.translatable("gui.randomthings.entity_detector.filter_label")
            .append(": ")
            .append(Component.translatable(f.translationKey()));
    }

    private Component invertLabel() {
        boolean inv = this.menu.getBlockEntity().isInvert();
        return Component.translatable("gui.randomthings.entity_detector.invert")
            .append(": ")
            .append(Component.literal(inv ? "ON" : "OFF").withStyle(inv ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private Component strongLabel() {
        boolean s = this.menu.getBlockEntity().isStrongOutput();
        return Component.translatable("gui.randomthings.entity_detector.strong")
            .append(": ")
            .append(Component.literal(s ? "ON" : "OFF").withStyle(s ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (filterButton != null) filterButton.setMessage(filterLabel());
        if (invertButton != null) invertButton.setMessage(invertLabel());
        if (strongOutputButton != null) strongOutputButton.setMessage(strongLabel());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);

        EntityDetectorBlockEntity be = this.menu.getBlockEntity();
        drawCentered(guiGraphics, "X: " + be.getRangeX(), 28, 44);
        drawCentered(guiGraphics, "Y: " + be.getRangeY(), 28, 60);
        drawCentered(guiGraphics, "Z: " + be.getRangeZ(), 28, 76);
    }

    private void drawCentered(GuiGraphics gui, String text, int leftEdge, int y) {
        int w = this.font.width(text);
        gui.drawString(this.font, text, leftEdge + (74 - w) / 2 + 4, y, 0x404040, false);
    }
}
