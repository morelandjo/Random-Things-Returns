package lumien.randomthings.client.screen;

import lumien.randomthings.menu.AdvancedRedstoneRepeaterMenu;
import lumien.randomthings.network.AdvancedRedstoneRepeaterUpdatePacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Hold Shift / Ctrl (or both) on a +/- button to step by 10 / 100 / 1000 instead of 1. */
public class AdvancedRedstoneRepeaterScreen extends AbstractContainerScreen<AdvancedRedstoneRepeaterMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("randomthings", "textures/gui/advancedredstonerepeater.png");

    public AdvancedRedstoneRepeaterScreen(AdvancedRedstoneRepeaterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 90;
        this.imageHeight = 56;
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> send(0)).bounds(this.leftPos + 5, this.topPos + 15, 10, 10).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> send(1)).bounds(this.leftPos + 75, this.topPos + 15, 10, 10).build());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> send(2)).bounds(this.leftPos + 5, this.topPos + 39, 10, 10).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> send(3)).bounds(this.leftPos + 75, this.topPos + 39, 10, 10).build());
    }

    private int getModifier() {
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();
        if (shift && ctrl) return 12;
        if (ctrl) return 8;
        if (shift) return 4;
        return 0;
    }

    private void send(int base) {
        RTNetwork.sendToServer(new AdvancedRedstoneRepeaterUpdatePacket(this.menu.getBlockEntity().getBlockPos(), base + getModifier()));
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
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_repeater.turn_off_delay"), 7, 5, 0x000000, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_repeater.turn_on_delay"), 9, 29, 0x000000, false);
        String on = String.valueOf(this.menu.getTurnOnDelay());
        guiGraphics.drawString(this.font, on, this.imageWidth / 2 - this.font.width(on) / 2, 40, 0x956400, false);
        String off = String.valueOf(this.menu.getTurnOffDelay());
        guiGraphics.drawString(this.font, off, this.imageWidth / 2 - this.font.width(off) / 2, 16, 0x956400, false);
    }
}
