package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.NotificationInterfaceBlockEntity;
import lumien.randomthings.menu.NotificationInterfaceMenu;
import lumien.randomthings.network.NotificationInterfaceUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class NotificationInterfaceScreen extends AbstractContainerScreen<NotificationInterfaceMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/notification_interface.png");
    
    private EditBox titleField;
    private EditBox descriptionField;

    public NotificationInterfaceScreen(NotificationInterfaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 146;
    }

    @Override
    protected void init() {
        super.init();
        
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        NotificationInterfaceBlockEntity blockEntity = this.menu.getBlockEntity();
        
        // Title field - adjusted positioning: moved up 2px (from 25 to 23)
        this.titleField = new EditBox(this.font, leftPos + 34, topPos + 23, 130, 15, Component.translatable("gui.randomthings.notification_interface.title"));
        this.titleField.setMaxLength(50);
        this.titleField.setBordered(true);
        this.titleField.setTextColor(0xFFFFFF); // White text for visibility on dark background
        this.titleField.setTextColorUneditable(0xA0A0A0);
        if (blockEntity != null) {
            String title = blockEntity.getTitle();
            this.titleField.setValue(title != null ? title.trim() : ""); // Trim any unwanted spaces
        }
        this.addWidget(this.titleField);
        
        // Description field - adjusted positioning: moved down 7px total (3px + 4px)
        this.descriptionField = new EditBox(this.font, leftPos + 34, topPos + 47, 130, 15, Component.translatable("gui.randomthings.notification_interface.description"));
        this.descriptionField.setMaxLength(100);
        this.descriptionField.setBordered(true);
        this.descriptionField.setTextColor(0xFFFFFF); // White text for visibility on dark background
        this.descriptionField.setTextColorUneditable(0xA0A0A0);
        if (blockEntity != null) {
            String description = blockEntity.getDescription();
            this.descriptionField.setValue(description != null ? description.trim() : ""); // Trim any unwanted spaces
        }
        this.addWidget(this.descriptionField);
    }
    
    private void saveSettings() {
        String title = this.titleField.getValue().trim(); // Trim spaces
        String description = this.descriptionField.getValue().trim(); // Trim spaces
        
        if (this.menu.getBlockEntity() != null) {
            PacketDistributor.sendToServer(new NotificationInterfaceUpdatePacket(
                this.menu.getBlockEntity().getBlockPos(), 
                title, 
                description
            ));
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.titleField.isFocused()) {
            if (this.titleField.keyPressed(keyCode, scanCode, modifiers)) {
                saveSettings();
                return true;
            }
            // Handle ESC to unfocus
            if (keyCode == 256) { // ESC key
                this.titleField.setFocused(false);
                return true;
            }
            // Block inventory key when typing
            if (this.minecraft != null && keyCode == this.minecraft.options.keyInventory.getKey().getValue()) {
                return true;
            }
            return true;
        }
        
        if (this.descriptionField.isFocused()) {
            if (this.descriptionField.keyPressed(keyCode, scanCode, modifiers)) {
                saveSettings();
                return true;
            }
            // Handle ESC to unfocus
            if (keyCode == 256) { // ESC key
                this.descriptionField.setFocused(false);
                return true;
            }
            // Block inventory key when typing
            if (this.minecraft != null && keyCode == this.minecraft.options.keyInventory.getKey().getValue()) {
                return true;
            }
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.titleField.isFocused() && this.titleField.charTyped(codePoint, modifiers)) {
            saveSettings();
            return true;
        }
        if (this.descriptionField.isFocused() && this.descriptionField.charTyped(codePoint, modifiers)) {
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
        
        // Ensure text fields have white text and render them
        this.titleField.setTextColor(0xFFFFFF); // White text
        this.descriptionField.setTextColor(0xFFFFFF); // White text
        this.titleField.render(guiGraphics, mouseX, mouseY, partialTick);
        this.descriptionField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw the main title at the top
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        
        // Draw field labels above the input fields (adjusted positioning)
        // Title label: moved down 7px total (3px + 4px) from original y=8 to y=15
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.notification_interface.title"), 34, 15, 4210752, false);
        // Description label: moved up 1px (from 40 to 39) 
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.notification_interface.description"), 34, 39, 4210752, false);
    }
}