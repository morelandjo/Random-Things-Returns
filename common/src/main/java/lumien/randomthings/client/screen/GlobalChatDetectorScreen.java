package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.menu.GlobalChatDetectorMenu;
import lumien.randomthings.network.GlobalChatDetectorUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class GlobalChatDetectorScreen extends AbstractContainerScreen<GlobalChatDetectorMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/global_chat_detector.png");

    private EditBox messageField;
    private boolean consumeMode = false;

    public GlobalChatDetectorScreen(GlobalChatDetectorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 157;
    }

    @Override
    protected void init() {
        super.init();

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        GlobalChatDetectorBlockEntity blockEntity = this.menu.getBlockEntity();
        this.consumeMode = blockEntity.isConsumeMessage();

        this.messageField = new EditBox(this.font, leftPos + 8, topPos + 29, 127, 15, Component.translatable("gui.randomthings.global_chat_detector.message"));
        this.messageField.setMaxLength(100);
        this.messageField.setValue(blockEntity.getDetectionMessage());
        this.messageField.setBordered(true);
        this.messageField.setTextColor(0xFFFFFF);
        this.messageField.setTextColorUneditable(0xA0A0A0);
        this.addWidget(this.messageField);

        this.addRenderableWidget(
            Button.builder(Component.literal(""), button -> toggleMode())
                .bounds(leftPos + 151, topPos + 14, 20, 20)
                .build()
        );
    }

    private void toggleMode() {
        this.consumeMode = !this.consumeMode;
        saveSettings();
    }

    private void saveSettings() {
        String message = this.messageField.getValue();
        RTNetwork.sendToServer(new GlobalChatDetectorUpdatePacket(
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
            if (keyCode == 256) { // ESC key
                this.messageField.setFocused(false);
                return true;
            }
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                return true;
            }
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

        guiGraphics.blit(BACKGROUND, leftPos, topPos + 9, 0, 0, this.imageWidth, this.imageHeight);

        this.messageField.setTextColor(0xFFFFFF);
        this.messageField.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        int buttonX = leftPos + 151;
        int buttonY = topPos + 14;

        boolean isHovering = mouseX >= buttonX && mouseX < buttonX + 20 && mouseY >= buttonY && mouseY < buttonY + 20;

        int textureX = this.consumeMode ? 196 : 176;
        int textureY = isHovering ? 20 : 0;
        guiGraphics.blit(BACKGROUND, buttonX, buttonY, textureX, textureY, 20, 20);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("block.randomthings.global_chat_detector"), 8, 15, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }
}
