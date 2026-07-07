package lumien.randomthings.client.screen;

import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.menu.SoundRecorderMenu;
import lumien.randomthings.network.RTNetwork;
import lumien.randomthings.network.SoundRecorderWritePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/** Lists the recorded sounds; click one to write it onto the inserted Sound Pattern. */
public class SoundRecorderScreen extends AbstractContainerScreen<SoundRecorderMenu> {
    private static final int LIST_X = 8;
    private static final int LIST_Y = 18;
    private static final int ROW_HEIGHT = 10;
    private int selected = -1;

    public SoundRecorderScreen(SoundRecorderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 202;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private List<ResourceLocation> sounds() {
        return SoundRecorderItem.getRecordedSounds(this.menu.getRecorderStack());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xC0101010);
        // Slot highlight for the pattern slot.
        guiGraphics.fill(this.leftPos + 79, this.topPos + 89, this.leftPos + 97, this.topPos + 107, 0xFF303030);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        List<ResourceLocation> sounds = sounds();
        for (int i = 0; i < sounds.size(); i++) {
            int y = this.topPos + LIST_Y + i * ROW_HEIGHT;
            int color = i == selected ? 0x55FF55 : 0xFFFFFF;
            String path = sounds.get(i).getPath();
            if (path.length() > 38) {
                path = "..." + path.substring(path.length() - 35);
            }
            guiGraphics.drawString(this.font, path, this.leftPos + LIST_X, y, color, false);
        }
        if (sounds.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.randomthings.sound_recorder.empty"),
                this.leftPos + LIST_X, this.topPos + LIST_Y, 0x808080, false);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<ResourceLocation> sounds = sounds();
        int localY = (int) mouseY - this.topPos - LIST_Y;
        int localX = (int) mouseX - this.leftPos;
        if (localX >= LIST_X && localX < this.imageWidth - 8 && localY >= 0 && localY < sounds.size() * ROW_HEIGHT) {
            int index = localY / ROW_HEIGHT;
            if (index >= 0 && index < sounds.size()) {
                selected = index;
                RTNetwork.sendToServer(new SoundRecorderWritePacket(index));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
