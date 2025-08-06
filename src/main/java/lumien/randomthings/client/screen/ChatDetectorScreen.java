package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.menu.ChatDetectorMenu;
import lumien.randomthings.network.ChatDetectorUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ChatDetectorScreen extends AbstractContainerScreen<ChatDetectorMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/chat_detector.png");
    
    private EditBox messageField;
    private Button consumeButton;
    private boolean consumeMode = false;

    public ChatDetectorScreen(ChatDetectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 136;
        this.imageHeight = 54;
        // Hide default labels since this is a compact GUI
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        ChatDetectorBlockEntity blockEntity = this.menu.getBlockEntity();
        this.consumeMode = blockEntity.isConsumeMessage();
        
        // Message input field - positioned at very bottom of GUI (54 - 18 = 36)
        this.messageField = new EditBox(this.font, leftPos + 5, topPos + 36, 105, 15, Component.translatable("gui.randomthings.chat_detector.message"));
        this.messageField.setMaxLength(100);
        this.messageField.setValue(blockEntity.getDetectionMessage());
        this.messageField.setBordered(true); // Need border for black background
        this.messageField.setTextColor(0xFFFFFF); // White text
        this.messageField.setTextColorUneditable(0xA0A0A0); // Gray when disabled
        this.addWidget(this.messageField);
        
        // Consume toggle button - matches original position (112, 5) with size 20x20
        this.consumeButton = this.addRenderableWidget(
            Button.builder(Component.literal(""), button -> toggleMode())
                .bounds(leftPos + 112, topPos + 5, 20, 20)
                .build()
        );
    }
    
    private void toggleMode() {
        this.consumeMode = !this.consumeMode;
        saveSettings();
    }
    
    private void saveSettings() {
        String message = this.messageField.getValue();
        PacketDistributor.sendToServer(new ChatDetectorUpdatePacket(
            this.menu.getBlockEntity().getBlockPos(), 
            message, 
            this.consumeMode
        ));
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.messageField.isFocused()) {
            if (this.messageField.keyPressed(keyCode, scanCode, modifiers)) {
                saveSettings();
                return true;
            }
            // Block ESC and inventory key when text field is focused, except for closing
            if (keyCode == 256) { // ESC key
                this.messageField.setFocused(false);
                return true;
            }
            // Block inventory key when typing
            if (this.minecraft != null && keyCode == this.minecraft.options.keyInventory.getKey().getValue()) {
                return true;
            }
            // Block other keys that might interfere
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.messageField.isFocused() && this.messageField.charTyped(codePoint, modifiers)) {
            saveSettings();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        // Draw main GUI background
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        
        // Button will be rendered in main render method with hover states
        
        // Ensure text field has correct color and render
        this.messageField.setTextColor(0xFFFFFF); // White text
        this.messageField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Re-render the button texture on top with hover state
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        int buttonX = leftPos + 112;
        int buttonY = topPos + 5;
        
        // Check if mouse is hovering over button for hover state
        boolean isHovering = mouseX >= buttonX && mouseX < buttonX + 20 && mouseY >= buttonY && mouseY < buttonY + 20;
        
        int textureX;
        int textureY;
        if (this.consumeMode) {
            textureX = 156; // On state texture
            textureY = isHovering ? 20 : 0; // Hover state below normal (y=20)
        } else {
            textureX = 136; // Off state texture  
            textureY = isHovering ? 20 : 0; // Hover state below normal (y=20)
        }
        guiGraphics.blit(BACKGROUND, buttonX, buttonY, textureX, textureY, 20, 20);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw the title
        guiGraphics.drawString(this.font, Component.translatable("block.randomthings.chat_detector"), 8, 6, 4210752, false);
    }
}