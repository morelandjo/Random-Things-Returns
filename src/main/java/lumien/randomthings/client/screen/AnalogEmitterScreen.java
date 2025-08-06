package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.menu.AnalogEmitterMenu;
import lumien.randomthings.network.AnalogEmitterPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnalogEmitterScreen extends AbstractContainerScreen<AnalogEmitterMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/analogemitter.png");
    
    private Button decreaseButton;
    private Button increaseButton;

    public AnalogEmitterScreen(AnalogEmitterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 78;
        this.imageHeight = 50;
    }

    @Override
    protected void init() {
        super.init();
        
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;
        
        this.decreaseButton = this.addRenderableWidget(
            Button.builder(Component.literal("-"), button -> changeLevel(-1))
                .bounds(leftPos + 5, topPos + this.imageHeight / 2 - 20 / 2 + 5, 20, 20)
                .build()
        );
        
        this.increaseButton = this.addRenderableWidget(
            Button.builder(Component.literal("+"), button -> changeLevel(1))
                .bounds(leftPos + 60 - 5, topPos + this.imageHeight / 2 - 20 / 2 + 5, 20, 20)
                .build()
        );
    }

    private void changeLevel(int delta) {
        AnalogEmitterBlockEntity blockEntity = this.menu.getBlockEntity();
        int currentLevel = blockEntity.getEmitLevel();
        int newLevel = currentLevel + delta;
        
        if (newLevel >= 1 && newLevel <= 15 && newLevel != currentLevel) {
            PacketDistributor.sendToServer(new AnalogEmitterPacket(blockEntity.getBlockPos(), newLevel));
        }
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
        guiGraphics.drawString(this.font, this.title, 4, 6, 4210752, false);
        
        AnalogEmitterBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity != null) {
            String levelText = String.valueOf(blockEntity.getEmitLevel());
            int stringWidth = this.font.width(levelText);
            int x = this.imageWidth / 2 - stringWidth / 2 + 3;
            int y = this.imageHeight / 2 - this.font.lineHeight / 2 + 5;
            guiGraphics.drawString(this.font, levelText, x, y, 4210752, false);
        }
    }
}