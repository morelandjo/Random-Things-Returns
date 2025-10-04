package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.widget.ToggleButton;
import lumien.randomthings.menu.ItemFilterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ItemFilterScreen extends AbstractContainerScreen<ItemFilterMenu> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/item_filter.png");

    private ToggleButton metadataButton;
    private ToggleButton tagsButton;
    private ToggleButton nbtButton;
    private ToggleButton listTypeButton;

    public ItemFilterScreen(ItemFilterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 220;
        this.imageHeight = 133;
        this.inventoryLabelY = -1000; // Hide inventory label
        this.titleLabelY = 6; // Position title at top
    }

    @Override
    protected void init() {
        super.init();

        // Metadata button (left top) - UV at (0, 133)
        metadataButton = new ToggleButton(
            leftPos + 173, topPos + 4,
            20, 20,
            TEXTURE, 0, 133,
            menu.getCheckMetadata(),
            Component.translatable("tooltip.randomthings.item_filter.nometadata"),
            Component.translatable("tooltip.randomthings.item_filter.metadata"),
            button -> this.menu.clickMenuButton(this.minecraft.player, 0)
        );
        addRenderableWidget(metadataButton);

        // Tags button (right top) - UV at (40, 133)
        tagsButton = new ToggleButton(
            leftPos + 195, topPos + 4,
            20, 20,
            TEXTURE, 40, 133,
            menu.getCheckTags(),
            Component.translatable("tooltip.randomthings.item_filter.notags"),
            Component.translatable("tooltip.randomthings.item_filter.tags"),
            button -> this.menu.clickMenuButton(this.minecraft.player, 1)
        );
        addRenderableWidget(tagsButton);

        // NBT button (left bottom) - UV at (80, 133)
        nbtButton = new ToggleButton(
            leftPos + 173, topPos + 26,
            20, 20,
            TEXTURE, 80, 133,
            menu.getCheckNBT(),
            Component.translatable("tooltip.randomthings.item_filter.nonbt"),
            Component.translatable("tooltip.randomthings.item_filter.nbt"),
            button -> this.menu.clickMenuButton(this.minecraft.player, 2)
        );
        addRenderableWidget(nbtButton);

        // List type button (right bottom) - UV at (120, 133)
        listTypeButton = new ToggleButton(
            leftPos + 195, topPos + 26,
            20, 20,
            TEXTURE, 120, 133,
            menu.getListType() != 0,
            Component.translatable("tooltip.randomthings.item_filter.whitelist"),
            Component.translatable("tooltip.randomthings.item_filter.blacklist"),
            button -> this.menu.clickMenuButton(this.minecraft.player, 3)
        );
        addRenderableWidget(listTypeButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw the main GUI background
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        // Render button tooltips
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
