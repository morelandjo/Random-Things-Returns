package lumien.randomthings.client.screen;

import lumien.randomthings.menu.BlockDestabilizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BlockDestabilizerScreen extends AbstractContainerScreen<BlockDestabilizerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/block_destabilizer.png");
    private static final ResourceLocation LAZY_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/block_destabilizer/lazy.png");
    private static final ResourceLocation FUZZY_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/block_destabilizer/fuzzy.png");
    
    private Button lazyButton;
    private Button fuzzyButton;
    private Button resetButton;

    public BlockDestabilizerScreen(BlockDestabilizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 85;
        this.imageHeight = 35;
        this.inventoryLabelY = -1000; // Hide inventory label
        this.titleLabelY = -1000; // Hide title label
    }

    @Override
    protected void init() {
        super.init();

        // Match original positions: lazy(7,7), fuzzy(33,7), reset(58,7)
        lazyButton = Button.builder(
                Component.literal("L"),
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
                    }
                })
                .bounds(leftPos + 7, topPos + 7, 20, 20)
                .build();
        addRenderableWidget(lazyButton);

        fuzzyButton = Button.builder(
                Component.literal("F"),
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                    }
                })
                .bounds(leftPos + 33, topPos + 7, 20, 20)
                .build();
        addRenderableWidget(fuzzyButton);

        resetButton = Button.builder(
                Component.literal("↻"),  // Unicode circular arrow symbol
                button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 2);
                    }
                })
                .bounds(leftPos + 58, topPos + 7, 20, 20)
                .build();
        addRenderableWidget(resetButton);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        
        // Update button text to show state
        boolean lazy = menu.getBlockEntity().isLazy();
        boolean fuzzy = menu.getBlockEntity().isFuzzy();
        
        lazyButton.setMessage(Component.literal(lazy ? "L*" : "L"));
        fuzzyButton.setMessage(Component.literal(fuzzy ? "F*" : "F"));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (lazyButton.isHoveredOrFocused() && mouseX >= lazyButton.getX() && mouseX < lazyButton.getX() + lazyButton.getWidth() &&
            mouseY >= lazyButton.getY() && mouseY < lazyButton.getY() + lazyButton.getHeight()) {
            guiGraphics.renderComponentTooltip(font, 
                java.util.List.of(
                    Component.literal("Lazy Mode:"),
                    Component.literal("Remembers the shape from"),
                    Component.literal("the last operation for"),
                    Component.literal("faster subsequent runs")
                ), mouseX, mouseY);
        }

        if (fuzzyButton.isHoveredOrFocused() && mouseX >= fuzzyButton.getX() && mouseX < fuzzyButton.getX() + fuzzyButton.getWidth() &&
            mouseY >= fuzzyButton.getY() && mouseY < fuzzyButton.getY() + fuzzyButton.getHeight()) {
            guiGraphics.renderComponentTooltip(font, 
                java.util.List.of(
                    Component.literal("Fuzzy Mode:"),
                    Component.literal("Only cares about block type,"),
                    Component.literal("not exact state"),
                    Component.literal("(e.g., Oak vs Wood Planks)")
                ), mouseX, mouseY);
        }

        if (resetButton.isHoveredOrFocused() && mouseX >= resetButton.getX() && mouseX < resetButton.getX() + resetButton.getWidth() &&
            mouseY >= resetButton.getY() && mouseY < resetButton.getY() + resetButton.getHeight()) {
            guiGraphics.renderComponentTooltip(font, 
                java.util.List.of(
                    Component.literal("Reset:"),
                    Component.literal("Clears the remembered"),
                    Component.literal("shape for lazy mode")
                ), mouseX, mouseY);
        }
    }
}