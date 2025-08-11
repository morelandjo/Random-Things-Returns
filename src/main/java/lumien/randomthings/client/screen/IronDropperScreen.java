package lumien.randomthings.client.screen;

import lumien.randomthings.blockentity.IronDropperBlockEntity;
import lumien.randomthings.menu.IronDropperMenu;
import lumien.randomthings.network.IronDropperPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class IronDropperScreen extends AbstractContainerScreen<IronDropperMenu> {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/iron_dropper.png");
    private static final ResourceLocation REDSTONE_MODE_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/iron_dropper/redstonemode.png");
    private static final ResourceLocation PICKUP_DELAY_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/iron_dropper/pickupdelay.png");
    private static final ResourceLocation RANDOM_MOTION_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/iron_dropper/randommotion.png");
    private static final ResourceLocation EFFECTS_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/iron_dropper/effects.png");

    private StateImageButton redstoneModeButton;
    private StateImageButton pickupDelayButton;
    private StateImageButton randomMotionButton;
    private StateImageButton effectsButton;

    public IronDropperScreen(IronDropperMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Redstone Mode Button (3 states) - using 20x20 button size
        redstoneModeButton = new StateImageButton(
            x + 125, y + 16, 20, 20, REDSTONE_MODE_TEXTURE, 3,
            (button) -> PacketDistributor.sendToServer(new IronDropperPacket(menu.getBlockEntity().getBlockPos(), 0)),
            Component.translatable("gui.randomthings.iron_dropper.redstone_mode")
        );
        addRenderableWidget(redstoneModeButton);

        // Pickup Delay Button (3 states) - using 20x20 button size
        pickupDelayButton = new StateImageButton(
            x + 150, y + 16, 20, 20, PICKUP_DELAY_TEXTURE, 3,
            (button) -> PacketDistributor.sendToServer(new IronDropperPacket(menu.getBlockEntity().getBlockPos(), 1)),
            Component.translatable("gui.randomthings.iron_dropper.pickup_delay")
        );
        addRenderableWidget(pickupDelayButton);

        // Random Motion Button (2 states) - using 20x20 button size
        randomMotionButton = new StateImageButton(
            x + 125, y + 41, 20, 20, RANDOM_MOTION_TEXTURE, 2,
            (button) -> PacketDistributor.sendToServer(new IronDropperPacket(menu.getBlockEntity().getBlockPos(), 2)),
            Component.translatable("gui.randomthings.iron_dropper.random_motion")
        );
        addRenderableWidget(randomMotionButton);

        // Effects Button (4 states) - using 20x20 button size  
        effectsButton = new StateImageButton(
            x + 150, y + 41, 20, 20, EFFECTS_TEXTURE, 4,
            (button) -> PacketDistributor.sendToServer(new IronDropperPacket(menu.getBlockEntity().getBlockPos(), 3)),
            Component.translatable("gui.randomthings.iron_dropper.effects")
        );
        addRenderableWidget(effectsButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Update button states based on block entity values
        IronDropperBlockEntity blockEntity = menu.getBlockEntity();
        redstoneModeButton.setState(blockEntity.getRedstoneMode().ordinal());
        pickupDelayButton.setState(blockEntity.getPickupDelay().ordinal());
        randomMotionButton.setState(blockEntity.isRandomMotion() ? 1 : 0);
        effectsButton.setState(blockEntity.getEffects().ordinal());

        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        IronDropperBlockEntity blockEntity = menu.getBlockEntity();

        // Redstone mode tooltip
        if (redstoneModeButton.isHovered()) {
            Component tooltip = switch (blockEntity.getRedstoneMode()) {
                case PULSE -> Component.translatable("tooltip.randomthings.iron_dropper.redstone_mode.pulse");
                case REPEAT_POWERED -> Component.translatable("tooltip.randomthings.iron_dropper.redstone_mode.repeat_powered");
                case REPEAT -> Component.translatable("tooltip.randomthings.iron_dropper.redstone_mode.repeat");
            };
            guiGraphics.renderTooltip(font, font.split(tooltip, 200), mouseX, mouseY);
        }

        // Pickup delay tooltip
        if (pickupDelayButton.isHovered()) {
            Component tooltip = switch (blockEntity.getPickupDelay()) {
                case NONE -> Component.translatable("tooltip.randomthings.iron_dropper.pickup_delay.none");
                case TICKS_5 -> Component.translatable("tooltip.randomthings.iron_dropper.pickup_delay.ticks_5");
                case TICKS_20 -> Component.translatable("tooltip.randomthings.iron_dropper.pickup_delay.ticks_20");
            };
            guiGraphics.renderTooltip(font, font.split(tooltip, 200), mouseX, mouseY);
        }

        // Random motion tooltip
        if (randomMotionButton.isHovered()) {
            Component tooltip = blockEntity.isRandomMotion() ? 
                Component.translatable("tooltip.randomthings.iron_dropper.random_motion.yes") :
                Component.translatable("tooltip.randomthings.iron_dropper.random_motion.no");
            guiGraphics.renderTooltip(font, font.split(tooltip, 200), mouseX, mouseY);
        }

        // Effects tooltip
        if (effectsButton.isHovered()) {
            Component tooltip = switch (blockEntity.getEffects()) {
                case NONE -> Component.translatable("tooltip.randomthings.iron_dropper.effects.none");
                case SOUND -> Component.translatable("tooltip.randomthings.iron_dropper.effects.sound");
                case PARTICLE -> Component.translatable("tooltip.randomthings.iron_dropper.effects.particle");
                case SOUND_PARTICLE -> Component.translatable("tooltip.randomthings.iron_dropper.effects.sound_particle");
            };
            guiGraphics.renderTooltip(font, font.split(tooltip, 200), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 6, 4210752, false);
        guiGraphics.drawString(font, playerInventoryTitle, 8, imageHeight - 96 + 2, 4210752, false);
    }

    // Custom button class that shows different visual states based on current setting
    private static class StateImageButton extends AbstractWidget {
        private final ResourceLocation texture;
        private final int stateCount;
        private final OnPress onPress;
        private int currentState = 0;

        public interface OnPress {
            void onPress(StateImageButton button);
        }

        public StateImageButton(int x, int y, int width, int height, ResourceLocation texture, int stateCount, OnPress onPress, Component message) {
            super(x, y, width, height, message);
            this.texture = texture;
            this.stateCount = stateCount;
            this.onPress = onPress;
        }

        public void setState(int state) {
            if (state >= 0 && state < stateCount) {
                this.currentState = state;
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.onPress.onPress(this);
            this.setFocused(false); // Remove focus to clear tooltip
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Texture layout: horizontal = different states, vertical = normal/hover
            // Each button in texture is 20x20 pixels
            // X coordinate: currentState * 20 (move horizontally through states)
            // Y coordinate: hover ? 20 : 0 (top row = normal, bottom row = hover)
            
            int textureX = currentState * 20; // Move horizontally through states
            int textureY = this.isHovered() ? 20 : 0; // Show hover effect only when mouse is currently over the button
            
            // Render the current state of the button
            guiGraphics.blit(texture, this.getX(), this.getY(), textureX, textureY, this.width, this.height);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.getMessage());
        }
    }
}