package lumien.randomthings.client.screen;

import lumien.randomthings.client.gui.InventoryTesterBoolButton;
import lumien.randomthings.menu.InventoryTesterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class InventoryTesterScreen extends AbstractContainerScreen<InventoryTesterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/inventory_tester.png");
    
    private InventoryTesterBoolButton invertButton;

    public InventoryTesterScreen(InventoryTesterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 136;
        // Position title text at x=33, y=6 as in original
        this.titleLabelX = 33;
        this.titleLabelY = 6;
        // Hide inventory label since it doesn't fit in this compact GUI
        this.inventoryLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();

        // Add invert signal button at position (93, 16) with size 20x20
        invertButton = new InventoryTesterBoolButton(
                leftPos + 93, topPos + 16,
                20, 20,
                () -> {
                    // Read directly from block entity like the original did
                    if (minecraft != null && minecraft.level != null) {
                        var blockEntity = minecraft.level.getBlockEntity(menu.getBlockEntity().getBlockPos());
                        if (blockEntity instanceof lumien.randomthings.blockentity.InventoryTesterBlockEntity inventoryTester) {
                            return inventoryTester.isInvertSignal();
                        }
                    }
                    return menu.getBlockEntity().isInvertSignal(); // Fallback
                },
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    }
                });
        addRenderableWidget(invertButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Show tooltip for invert button
        if (invertButton.isHoveredOrFocused()) {
            // Read directly from block entity like original did
            boolean inverted = false;
            if (minecraft != null && minecraft.level != null) {
                var blockEntity = minecraft.level.getBlockEntity(menu.getBlockEntity().getBlockPos());
                if (blockEntity instanceof lumien.randomthings.blockentity.InventoryTesterBlockEntity inventoryTester) {
                    inverted = inventoryTester.isInvertSignal();
                }
            }
            
            Component tooltipText = inverted ? 
                Component.translatable("gui.randomthings.inventory_tester.invert_signal.yes") :
                Component.translatable("gui.randomthings.inventory_tester.invert_signal.no");
            
            guiGraphics.renderComponentTooltip(font, java.util.List.of(tooltipText), mouseX, mouseY);
        }
    }
}