package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.IgniterBlockEntity;
import lumien.randomthings.menu.IgniterMenu;
import lumien.randomthings.network.IgniterPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class IgniterScreen extends AbstractContainerScreen<IgniterMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/igniter.png");
    
    private Button modeButton;

    public IgniterScreen(IgniterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 78;
        this.imageHeight = 50;
        
        // Hide default labels as per migration guide for compact GUIs
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();
        
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        this.modeButton = this.addRenderableWidget(
            Button.builder(getModeButtonText(), button -> cycleModeButton())
                .bounds(leftPos + 5, topPos + 4, 68, 20)
                .build()
        );
    }

    private void cycleModeButton() {
        IgniterBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity != null) {
            PacketDistributor.sendToServer(new IgniterPacket(blockEntity.getBlockPos()));
        }
    }

    private Component getModeButtonText() {
        IgniterBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity != null) {
            return Component.translatable(blockEntity.getMode().getTranslationKey());
        }
        return Component.translatable("gui.randomthings.igniter.toggle");
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // No labels to render for this compact GUI
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        
        // Update button text every tick to reflect server state
        if (this.modeButton != null) {
            Component currentText = this.modeButton.getMessage();
            Component expectedText = getModeButtonText();
            
            if (!currentText.equals(expectedText)) {
                this.modeButton.setMessage(expectedText);
            }
        }
    }
}