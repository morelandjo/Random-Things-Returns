package lumien.randomthings.client.screen;

import lumien.randomthings.client.ChunkAnalyzerClientData;
import lumien.randomthings.menu.ChunkAnalyzerMenu;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Lists the blocks found in the scanned chunk (icon + name + count), scrollable with the mouse wheel. */
public class ChunkAnalyzerScreen extends AbstractContainerScreen<ChunkAnalyzerMenu> {
    private static final int ROW_HEIGHT = 20;
    private static final int VISIBLE_ROWS = 9;
    private int scroll = 0;

    public ChunkAnalyzerScreen(ChunkAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 20 + VISIBLE_ROWS * ROW_HEIGHT + 8;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.inventoryLabelY = this.imageHeight;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xC0101010);
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + 18, 0xFF202020);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = this.leftPos + 8;
        int yTop = this.topPos + 22;
        if (this.menu.isScanning()) {
            guiGraphics.drawString(this.font, Component.translatable("item.randomthings.chunk_analyzer.scanning"), x, yTop, 0xFFFFFF, false);
            return;
        }
        ChunkAnalyzerResult result = ChunkAnalyzerClientData.getLatestResult();
        int total = result.size();
        int maxScroll = Math.max(0, total - VISIBLE_ROWS);
        if (scroll > maxScroll) {
            scroll = maxScroll;
        }
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int index = scroll + row;
            if (index >= total) {
                break;
            }
            int rowY = yTop + row * ROW_HEIGHT;
            guiGraphics.renderItem(result.displayStacks().get(index), x, rowY);
            String label = result.blockDescriptions().get(index) + "  x" + result.blockCounts().get(index);
            guiGraphics.drawString(this.font, label, x + 22, rowY + 4, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int total = ChunkAnalyzerClientData.getLatestResult().size();
        int maxScroll = Math.max(0, total - VISIBLE_ROWS);
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) Math.signum(delta)));
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, 6, 0xFFFFFF, false);
    }
}
