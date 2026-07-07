package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.menu.AnalogEmitterMenu;
import lumien.randomthings.network.AnalogEmitterUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AnalogEmitterScreen extends AbstractContainerScreen<AnalogEmitterMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/analogemitter.png");

    public AnalogEmitterScreen(AnalogEmitterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 78;
        this.imageHeight = 50;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("-"), button -> changeLevel(-1))
            .bounds(this.leftPos + 5, this.topPos + this.imageHeight / 2 - 5, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> changeLevel(1))
            .bounds(this.leftPos + 55, this.topPos + this.imageHeight / 2 - 5, 20, 20).build());
    }

    private void changeLevel(int delta) {
        AnalogEmitterBlockEntity blockEntity = this.menu.getBlockEntity();
        int newLevel = blockEntity.getEmitLevel() + delta;
        if (newLevel >= 1 && newLevel <= 15) {
            RTNetwork.sendToServer(new AnalogEmitterUpdatePacket(blockEntity.getBlockPos(), newLevel));
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 4, 6, 4210752, false);
        AnalogEmitterBlockEntity blockEntity = this.menu.getBlockEntity();
        if (blockEntity != null) {
            String levelText = String.valueOf(blockEntity.getEmitLevel());
            int x = this.imageWidth / 2 - this.font.width(levelText) / 2 + 3;
            int y = this.imageHeight / 2 - this.font.lineHeight / 2 + 5;
            guiGraphics.drawString(this.font, levelText, x, y, 4210752, false);
        }
    }
}
