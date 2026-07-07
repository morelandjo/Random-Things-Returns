package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.AdvancedItemCollectorBlockEntity;
import lumien.randomthings.menu.AdvancedItemCollectorMenu;
import lumien.randomthings.network.AdvancedItemCollectorPacket;
import lumien.randomthings.network.RTNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class AdvancedItemCollectorScreen extends AbstractContainerScreen<AdvancedItemCollectorMenu> {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("randomthings", "textures/gui/advanced_item_collector.png");

    public AdvancedItemCollectorScreen(AdvancedItemCollectorMenu menu, Inventory playerInventory, Component title) {
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
        addRangeRow(x, y + 22, 0);
        addRangeRow(x, y + 42, 1);
        addRangeRow(x, y + 62, 2);
    }

    private void addRangeRow(int leftPos, int rowY, int axis) {
        AdvancedItemCollectorBlockEntity be = this.menu.getBlockEntity();
        this.addRenderableWidget(
            Button.builder(Component.literal("-"), btn -> send(axis, currentRange(be, axis) - 1))
                .bounds(leftPos + 8, rowY, 16, 16).build());
        this.addRenderableWidget(
            Button.builder(Component.literal("+"), btn -> send(axis, currentRange(be, axis) + 1))
                .bounds(leftPos + 82, rowY, 16, 16).build());
    }

    private static int currentRange(AdvancedItemCollectorBlockEntity be, int axis) {
        return switch (axis) {
            case 0 -> be.getRangeX();
            case 1 -> be.getRangeY();
            case 2 -> be.getRangeZ();
            default -> 0;
        };
    }

    private void send(int axis, int value) {
        RTNetwork.sendToServer(new AdvancedItemCollectorPacket(
            this.menu.getBlockEntity().getBlockPos(), axis, value));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040, false);

        AdvancedItemCollectorBlockEntity be = this.menu.getBlockEntity();
        drawCentered(guiGraphics, "X: " + be.getRangeX(), 28, 26);
        drawCentered(guiGraphics, "Y: " + be.getRangeY(), 28, 46);
        drawCentered(guiGraphics, "Z: " + be.getRangeZ(), 28, 66);
    }

    private void drawCentered(GuiGraphics gui, String text, int leftEdge, int y) {
        int w = this.font.width(text);
        gui.drawString(this.font, text, leftEdge + (74 - w) / 2 + 4, y, 0x404040, false);
    }
}
