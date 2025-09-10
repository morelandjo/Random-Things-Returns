package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.widget.ScanResultList;
import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.menu.ChunkAnalyzerMenu;
import lumien.randomthings.network.ChunkAnalyzerPacket;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChunkAnalyzerScreen extends AbstractContainerScreen<ChunkAnalyzerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/chunk_analyzer.png");
    
    private ScanResultList scanResultList;
    private Button scanButton;
    private int animationCounter = 0;
    private ChunkAnalyzerResult lastResults = null;

    public ChunkAnalyzerScreen(ChunkAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 190;
        this.imageHeight = 124;
        this.inventoryLabelY = -1000; // Hide inventory label
        this.titleLabelY = 6; // Position title at the top
    }

    @Override
    protected void init() {
        super.init();

        // Add scan button - positioned like in the original (136, 5) relative to GUI
        scanButton = Button.builder(
                Component.literal("Scan"),
                button -> {
                    // Send packet to start scanning
                    RTPacketHandler.sendToServer(new ChunkAnalyzerPacket(ChunkAnalyzerPacket.Action.START));
                })
                .bounds(leftPos + 136, topPos + 5, 50, 14)
                .build();
        addRenderableWidget(scanButton);

        // Create scan result list - positioned like in the original (4, 20) relative to GUI, size 182x100
        scanResultList = new ScanResultList(leftPos + 4, topPos + 20, 182, 100);
        addRenderableWidget(scanResultList);

        // Load existing results if any
        updateResults();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        animationCounter++;
        
        // Update scan button state
        boolean isScanning = menu.isScanning();
        scanButton.active = !isScanning;
        
        // Update results display
        updateResults();
    }

    private void updateResults() {
        ItemStack analyzerStack = menu.getAnalyzerStack();
        ChunkAnalyzerResult results = ChunkAnalyzerItem.getResults(analyzerStack);
        
        // Only update if results have actually changed to prevent scroll reset
        if (results != lastResults) {
            scanResultList.setResults(results);
            lastResults = results;
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw the main GUI background
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render scanning animation if scanning
        if (menu.isScanning()) {
            renderScanningAnimation(guiGraphics);
        }
    }

    private void renderScanningAnimation(GuiGraphics guiGraphics) {
        String scanningText = "Scanning Chunk";
        
        // Add animated dots (similar to original implementation)
        int dots = (animationCounter / 15) % 4; // Cycle every 60 ticks (3 seconds at 20 TPS)
        for (int i = 0; i < dots; i++) {
            scanningText += ".";
        }
        
        // Center the text horizontally in the results area
        int textWidth = font.width(scanningText);
        int textX = leftPos + 95 - (textWidth / 2); // Center in the 190px wide GUI
        int textY = topPos + 65; // Position like in original
        
        guiGraphics.drawString(font, scanningText, textX, textY, 0xFFFFFF);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Forward scroll events to the result list
        if (scanResultList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}