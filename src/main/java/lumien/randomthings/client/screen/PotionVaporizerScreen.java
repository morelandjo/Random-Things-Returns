package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.PotionVaporizerBlockEntity;
import lumien.randomthings.menu.PotionVaporizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class PotionVaporizerScreen extends AbstractContainerScreen<PotionVaporizerMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/potionvaporizer.png");

    public PotionVaporizerScreen(PotionVaporizerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        // Draw main background
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Get data from synchronized container data
        int duration = this.menu.getDuration();
        int durationLeft = this.menu.getDurationLeft();
        int color = this.menu.getColor();
        int fuelBurn = this.menu.getFuelBurn();
        int fuelBurnTime = this.menu.getFuelBurnTime();

        // Draw potion tank with colored liquid
        if (duration != 0) {
            int tankProgress = (int) Math.floor(14F - (14F / duration * durationLeft));

            // Extract RGB components
            int red = FastColor.ARGB32.red(color);
            int green = FastColor.ARGB32.green(color);
            int blue = FastColor.ARGB32.blue(color);

            float r = red / 255.0F;
            float g = green / 255.0F;
            float b = blue / 255.0F;

            // Draw colored potion liquid
            guiGraphics.setColor(r, g, b, 1.0F);
            guiGraphics.blit(BACKGROUND, leftPos + 81, topPos + 18 + tankProgress, 
                           176, 30, 14, 14 - tankProgress);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // Draw potion tank overlay
        guiGraphics.blit(BACKGROUND, leftPos + 80, topPos + 17, 176, 14, 16, 16);

        // Draw fuel burn progress
        if (fuelBurnTime > 0 && fuelBurn > 0) {
            // Use the original 1.12.2 calculation for consistency
            int fuelProgress = 15 - (int) Math.floor(14F - (14F / fuelBurn * fuelBurnTime));
            guiGraphics.blit(BACKGROUND, leftPos + 81, topPos + 50 - fuelProgress, 
                           176, 14 - fuelProgress, 14, fuelProgress);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int leftPos = (this.width - this.imageWidth) / 2;
        int topPos = (this.height - this.imageHeight) / 2;

        // Check if mouse is over potion tank area
        int relativeX = mouseX - leftPos;
        int relativeY = mouseY - topPos;

        if (relativeX >= 79 && relativeX <= 96 && relativeY >= 16 && relativeY <= 33) {
            int duration = this.menu.getDuration();
            int durationLeft = this.menu.getDurationLeft();

            if (duration != 0) {
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.randomthings.potion_vaporizer.active_potion"));
                
                // Format remaining time
                int ticks = durationLeft;
                int minutes = ticks / 1200; // 20 ticks/sec * 60 sec/min = 1200 ticks/min
                int seconds = (ticks % 1200) / 20;
                
                String timeString = String.format("%d:%02d", minutes, seconds);
                tooltip.add(Component.literal(timeString));
                
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }
}