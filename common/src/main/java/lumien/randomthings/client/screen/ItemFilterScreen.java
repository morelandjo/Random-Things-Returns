package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.widget.ToggleButton;
import lumien.randomthings.menu.ItemFilterMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class ItemFilterScreen extends AbstractContainerScreen<ItemFilterMenu> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation("randomthings", "textures/gui/item_filter.png");

    private ToggleButton metadataButton;
    private ToggleButton tagsButton;
    private ToggleButton nbtButton;
    private ToggleButton listTypeButton;

    public ItemFilterScreen(ItemFilterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 133;
        this.inventoryLabelY = -1000;
        this.titleLabelY = 6;
    }

    private void clickButton(int id) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void init() {
        super.init();

        metadataButton = new ToggleButton(
            leftPos + 173, topPos + 4, 20, 20,
            TEXTURE, 0, 133, menu.getCheckMetadata(),
            Component.translatable("tooltip.randomthings.item_filter.nometadata"),
            Component.translatable("tooltip.randomthings.item_filter.metadata"),
            button -> clickButton(0));
        addRenderableWidget(metadataButton);

        tagsButton = new ToggleButton(
            leftPos + 195, topPos + 4, 20, 20,
            TEXTURE, 40, 133, menu.getCheckTags(),
            Component.translatable("tooltip.randomthings.item_filter.notags"),
            Component.translatable("tooltip.randomthings.item_filter.tags"),
            button -> clickButton(1));
        addRenderableWidget(tagsButton);

        nbtButton = new ToggleButton(
            leftPos + 173, topPos + 26, 20, 20,
            TEXTURE, 80, 133, menu.getCheckNBT(),
            Component.translatable("tooltip.randomthings.item_filter.nonbt"),
            Component.translatable("tooltip.randomthings.item_filter.nbt"),
            button -> clickButton(2));
        addRenderableWidget(nbtButton);

        listTypeButton = new ToggleButton(
            leftPos + 195, topPos + 26, 20, 20,
            TEXTURE, 120, 133, menu.getListType() != 0,
            Component.translatable("tooltip.randomthings.item_filter.whitelist"),
            Component.translatable("tooltip.randomthings.item_filter.blacklist"),
            button -> clickButton(3));
        addRenderableWidget(listTypeButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (metadataButton.isHovered()) {
            guiGraphics.renderTooltip(font, metadataButton.getTooltipComponent(), mouseX, mouseY);
        } else if (tagsButton.isHovered()) {
            guiGraphics.renderTooltip(font, tagsButton.getTooltipComponent(), mouseX, mouseY);
        } else if (nbtButton.isHovered()) {
            guiGraphics.renderTooltip(font, nbtButton.getTooltipComponent(), mouseX, mouseY);
        } else if (listTypeButton.isHovered()) {
            guiGraphics.renderTooltip(font, listTypeButton.getTooltipComponent(), mouseX, mouseY);
        }
    }
}
