package lumien.randomthings.client.screen;

import lumien.randomthings.menu.ImbuingStationMenu;
import lumien.randomthings.recipe.ImbuingRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Mirrors upstream GuiImbuingStation: 176x208 background, animated bubble overlays around the three
 * ingredient slots and a horizontal progress arrow leading to the output slot.
 */
@Environment(EnvType.CLIENT)
public class ImbuingStationScreen extends AbstractContainerScreen<ImbuingStationMenu> {

    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("randomthings", "textures/gui/imbuing_station.png");

    private int bubbleAnim = 0;

    public ImbuingStationScreen(ImbuingStationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 208;
        this.titleLabelX = 3;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 115;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (menu.getImbuingProgress() > 0) {
            bubbleAnim = (bubbleAnim + 2) % 26;
        } else {
            bubbleAnim = 0;
        }
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        gg.blit(BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        int progress = menu.getImbuingProgress();
        if (progress <= 0) return;

        int arrow = Math.min((int) (22F / ImbuingRecipe.CRAFT_TICKS * progress) + 1, 22);
        gg.blit(BACKGROUND, x + 99, y + 54, 189, 13, arrow, 16, 256, 256);

        int b = Math.min(bubbleAnim, 24);

        if (!menu.slots.get(ImbuingRecipe.SLOT_INGREDIENT_1).getItem().isEmpty()) {
            gg.blit(BACKGROUND, x + 82, y + 28, 176, 0, 12, b, 256, 256);
        }
        if (!menu.slots.get(ImbuingRecipe.SLOT_INGREDIENT_2).getItem().isEmpty()) {
            gg.blit(BACKGROUND, x + 54, y + 56, 189, 0, b, 12, 256, 256);
        }
        if (!menu.slots.get(ImbuingRecipe.SLOT_INGREDIENT_3).getItem().isEmpty()) {
            gg.blit(BACKGROUND, x + 82, y + 72, 176, 25, 12, 24, 256, 256);
            int drain = Math.max(24 - b, 0);
            gg.blit(BACKGROUND, x + 82, y + 72, 176, 50, 12, drain, 256, 256);
        }
    }
}
