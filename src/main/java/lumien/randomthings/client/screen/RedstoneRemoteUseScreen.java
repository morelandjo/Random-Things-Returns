package lumien.randomthings.client.screen;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.menu.RedstoneRemoteUseMenu;
import lumien.randomthings.network.RedstoneRemotePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for using the Redstone Remote with dynamic buttons
 */
public class RedstoneRemoteUseScreen extends AbstractContainerScreen<RedstoneRemoteUseMenu> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/redstoneremote/redstoneremoteuse.png");

    private final List<ItemButtonWidget> itemButtons = new ArrayList<>();

    public RedstoneRemoteUseScreen(RedstoneRemoteUseMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 187;
        this.imageHeight = 41;
    }

    @Override
    protected void init() {
        super.init();
        itemButtons.clear();

        Container remoteInventory = menu.getRemoteInventory();
        if (remoteInventory != null) {
            for (int i = 0; i < 9; i++) {
                ItemStack positionFilter = remoteInventory.getItem(i);

                if (!positionFilter.isEmpty() && positionFilter.is(ModItems.POSITION_FILTER.get())) {
                    // Check if position is set
                    if (PositionFilterItem.hasPosition(positionFilter)) {
                        // Get display icon (ghost slot) or use position filter
                        ItemStack displayIcon = remoteInventory.getItem(i + 9);
                        if (displayIcon.isEmpty()) {
                            displayIcon = positionFilter;
                        }

                        // Get custom name from position filter for tooltip
                        Component customName = null;
                        if (positionFilter.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                            customName = positionFilter.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
                        }

                        int buttonX = this.leftPos + 5 + i * 20;
                        int buttonY = this.topPos + 17;

                        ItemButtonWidget button = new ItemButtonWidget(
                            buttonX, buttonY, displayIcon, customName, i, this::onButtonClick);
                        addRenderableWidget(button);
                        itemButtons.add(button);
                    }
                }
            }
        }
    }

    private void onButtonClick(int slotId) {
        // Send packet to server
        PacketDistributor.sendToServer(new RedstoneRemotePayload(menu.getHand(), slotId));
        this.onClose();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Custom button widget that displays an item icon
     */
    private static class ItemButtonWidget extends Button {
        private final ItemStack displayItem;
        private final Component tooltip;
        private final int slotId;
        private final java.util.function.IntConsumer onPressConsumer;

        public ItemButtonWidget(int x, int y, ItemStack displayItem, Component tooltip, int slotId,
                                java.util.function.IntConsumer onPress) {
            super(x, y, 18, 18, Component.empty(), button -> {}, DEFAULT_NARRATION);
            this.displayItem = displayItem;
            this.tooltip = tooltip;
            this.slotId = slotId;
            this.onPressConsumer = onPress;
        }

        @Override
        public void onPress() {
            onPressConsumer.accept(slotId);
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Render button background (slot-like)
            graphics.fill(this.getX(), this.getY(), this.getX() + 18, this.getY() + 18, 0xFF8B8B8B);
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + 17, this.getY() + 17, 0xFF373737);

            // Render item
            if (!displayItem.isEmpty()) {
                graphics.renderItem(displayItem, this.getX() + 1, this.getY() + 1);
                graphics.renderItemDecorations(Minecraft.getInstance().font, displayItem,
                    this.getX() + 1, this.getY() + 1);
            }

            // Render hover effect
            if (this.isHoveredOrFocused()) {
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + 17, this.getY() + 17, 0x80FFFFFF);

                // Render tooltip
                if (tooltip != null) {
                    graphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
                }
            }
        }
    }
}
