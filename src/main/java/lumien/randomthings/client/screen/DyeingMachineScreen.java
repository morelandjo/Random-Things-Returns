package lumien.randomthings.client.screen;

import lumien.randomthings.menu.DyeingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DyeingMachineScreen extends AbstractContainerScreen<DyeingMachineMenu> {
    private static final ResourceLocation BACKGROUND =
        ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/dyeing_machine.png");

    public DyeingMachineScreen(DyeingMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 141;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
