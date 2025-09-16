package lumien.randomthings.client.screen;

import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.EnderLetterMenu;
import lumien.randomthings.network.EnderLetterUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EnderLetterScreen extends AbstractContainerScreen<EnderLetterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/enderletter.png");

    private EditBox receiverField;
    private String oldReceiver = "";

    public EnderLetterScreen(EnderLetterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    protected void init() {
        super.init();

        // Receiver name input field
        this.receiverField = new EditBox(this.font, this.leftPos + 92, this.topPos + 5, 76, 10, Component.literal(""));
        this.receiverField.setMaxLength(16);
        this.receiverField.setValue(menu.getReceiver() != null ? menu.getReceiver() : "");
        this.receiverField.setEditable(!menu.isSigned());
        this.receiverField.setVisible(true);
        this.receiverField.setCanLoseFocus(true);
        this.oldReceiver = this.receiverField.getValue();
        this.addRenderableWidget(this.receiverField);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Auto-save receiver name when it changes (like original mod)
        if (!oldReceiver.equals(receiverField.getValue())) {
            oldReceiver = receiverField.getValue();
            menu.setReceiver(oldReceiver);
            // Send packet to server to update receiver name
            PacketDistributor.sendToServer(new EnderLetterUpdatePacket(oldReceiver));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Render the text field
        this.receiverField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Draw title
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Always handle ESC key to close GUI
        if (keyCode == 256) { // ESC key
            this.onClose();
            return true;
        }

        // If text field is focused, handle input there first and block other keybinds
        if (receiverField.isFocused()) {
            if (receiverField.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            // Block all other keys when text field is focused (including inventory key)
            return true;
        }

        // Only check inventory key when text field is NOT focused
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (receiverField.isFocused() && receiverField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Handle text field mouse clicks first
        if (receiverField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(receiverField);
            return true;
        }

        // If clicking outside the text field, remove focus
        if (receiverField.isFocused()) {
            receiverField.setFocused(false);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        // Ensure receiver name is saved when GUI is closed
        if (receiverField != null && !oldReceiver.equals(receiverField.getValue())) {
            menu.setReceiver(receiverField.getValue());
            PacketDistributor.sendToServer(new EnderLetterUpdatePacket(receiverField.getValue()));
        }
        super.onClose();
    }
}