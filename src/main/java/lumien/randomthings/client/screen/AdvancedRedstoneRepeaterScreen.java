package lumien.randomthings.client.screen;

import lumien.randomthings.menu.AdvancedRedstoneRepeaterMenu;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdvancedRedstoneRepeaterScreen extends AbstractContainerScreen<AdvancedRedstoneRepeaterMenu> {
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/advancedredstonerepeater.png");

    public AdvancedRedstoneRepeaterScreen(AdvancedRedstoneRepeaterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        
        this.imageWidth = 90;
        this.imageHeight = 56;
    }

    @Override
    protected void init() {
        super.init();

        // Turn Off Delay controls
        this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
            int mod = getModifier();
            sendButtonPress(0 + mod);
        }).bounds(this.leftPos + 5, this.topPos + 15, 10, 10).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
            int mod = getModifier();
            sendButtonPress(1 + mod);
        }).bounds(this.leftPos + 5 + 70, this.topPos + 15, 10, 10).build());

        // Turn On Delay controls
        this.addRenderableWidget(Button.builder(Component.literal("-"), (button) -> {
            int mod = getModifier();
            sendButtonPress(2 + mod);
        }).bounds(this.leftPos + 5, this.topPos + 39, 10, 10).build());

        this.addRenderableWidget(Button.builder(Component.literal("+"), (button) -> {
            int mod = getModifier();
            sendButtonPress(3 + mod);
        }).bounds(this.leftPos + 5 + 70, this.topPos + 39, 10, 10).build());
    }

    private int getModifier() {
        boolean shift = hasShiftDown();
        boolean ctrl = hasControlDown();
        
        if (shift && ctrl) {
            return 12; // 1000 step
        } else if (ctrl) {
            return 8; // 100 step
        } else if (shift) {
            return 4; // 10 step
        }
        return 0; // 1 step
    }

    private void sendButtonPress(int buttonId) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buffer.writeInt(buttonId);
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        PacketDistributor.sendToServer(new ContainerSignalMessage(0, data));
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_repeater.turn_off_delay"), 7, 5, 0x000000, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.advanced_redstone_repeater.turn_on_delay"), 9, 29, 0x000000, false);

        if (this.menu instanceof AdvancedRedstoneRepeaterMenu arrMenu) {
            String turnOnDelayString = arrMenu.getTurnOnDelay() + "";
            int onStringWidth = this.font.width(turnOnDelayString);
            guiGraphics.drawString(this.font, turnOnDelayString, 
                imageWidth / 2 - onStringWidth / 2, 40, 0x956400, false);

            String turnOffDelayString = arrMenu.getTurnOffDelay() + "";
            int offStringWidth = this.font.width(turnOffDelayString);
            guiGraphics.drawString(this.font, turnOffDelayString, 
                imageWidth / 2 - offStringWidth / 2, 16, 0x956400, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}