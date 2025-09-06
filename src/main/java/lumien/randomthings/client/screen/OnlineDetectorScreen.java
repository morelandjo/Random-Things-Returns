package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.OnlineDetectorBlockEntity;
import lumien.randomthings.menu.OnlineDetectorMenu;
// import lumien.randomthings.network.OnlineDetectorUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class OnlineDetectorScreen extends AbstractContainerScreen<OnlineDetectorMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/online_detector.png");
    
    private EditBox usernameField;

    public OnlineDetectorScreen(OnlineDetectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 136;
        this.imageHeight = 52;
        // Hide default labels since this is a compact GUI
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        OnlineDetectorBlockEntity blockEntity = this.menu.getBlockEntity();
        
        // Username input field - positioned near bottom of GUI
        this.usernameField = new EditBox(this.font, leftPos + 5, topPos + 30, 127, 15, Component.translatable("gui.randomthings.online_detector.username"));
        this.usernameField.setMaxLength(50); // Reasonable username length limit
        this.usernameField.setValue(blockEntity.getUsername());
        this.usernameField.setBordered(true);
        this.usernameField.setTextColor(0xFFFFFF); // White text
        this.usernameField.setTextColorUneditable(0xA0A0A0); // Gray when disabled
        this.addWidget(this.usernameField);
    }
    
    private void saveSettings() {
        String username = this.usernameField.getValue();
        // PacketDistributor.sendToServer(new OnlineDetectorUpdatePacket(
        //     this.menu.getBlockEntity().getBlockPos(), 
        //     username
        // ));
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.usernameField.isFocused()) {
            if (this.usernameField.keyPressed(keyCode, scanCode, modifiers)) {
                saveSettings();
                return true;
            }
            // Block ESC and inventory key when text field is focused, except for closing
            if (keyCode == 256) { // ESC key
                this.usernameField.setFocused(false);
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
        if (this.usernameField.isFocused() && this.usernameField.charTyped(codePoint, modifiers)) {
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
        
        // Ensure text field has correct color and render
        this.usernameField.setTextColor(0xFFFFFF); // White text
        this.usernameField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw the title
        guiGraphics.drawString(this.font, Component.translatable("block.randomthings.online_detector"), 8, 6, 4210752, false);
    }
}