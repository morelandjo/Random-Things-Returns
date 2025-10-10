package lumien.randomthings.client.screen;

import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.menu.SoundRecorderMenu;
import lumien.randomthings.network.SoundSelectedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen for the Sound Recorder item
 * Displays a scrollable list of recorded sounds and allows saving them to Sound Pattern items
 */
public class SoundRecorderScreen extends AbstractContainerScreen<SoundRecorderMenu> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("randomthings", "textures/gui/sound_recorder.png");

    private SoundListWidget soundList;
    private List<ResourceLocation> recordedSounds;

    public SoundRecorderScreen(SoundRecorderMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 190;
        this.imageHeight = 186;
        this.inventoryLabelY = this.imageHeight - 94;

        // Get recorded sounds from the recorder item
        ItemStack recorderStack = menu.getRecorderStack();
        if (recorderStack != null && !recorderStack.isEmpty()) {
            this.recordedSounds = recorderStack.getOrDefault(
                ModDataComponents.SOUND_RECORDER_SOUNDS.get(),
                new ArrayList<>()
            );
        } else {
            this.recordedSounds = new ArrayList<>();
        }
    }

    @Override
    protected void init() {
        super.init();

        // Create scrollable sound list widget
        int listX = this.leftPos + 5;
        int listY = this.topPos + 17;
        int listWidth = 180;
        int listHeight = 50;

        this.soundList = new SoundListWidget(
            this.minecraft,
            listWidth,
            listHeight,
            listX,
            listY,
            recordedSounds,
            this::onSoundSelected
        );

        addRenderableWidget(this.soundList);
    }

    /**
     * Called when a sound is selected from the list
     */
    private void onSoundSelected(ResourceLocation selectedSound) {
        // Send packet to server to create Sound Pattern
        PacketDistributor.sendToServer(new SoundSelectedPacket(selectedSound));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw title at top
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        // Draw inventory label
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Custom scrollable list widget for displaying recorded sounds
     */
    private static class SoundListWidget extends AbstractWidget {
        private final Minecraft minecraft;
        private final int listWidth;
        private final int listHeight;
        private final int x;
        private final int y;
        private final List<ResourceLocation> sounds;
        private final java.util.function.Consumer<ResourceLocation> onSelect;

        private int scrollOffset = 0;
        private int hoveredIndex = -1;

        private static final int ENTRY_HEIGHT = 10; // Font height
        private static final int SCROLL_SPEED = 1;

        public SoundListWidget(Minecraft minecraft, int width, int height, int x, int y,
                               List<ResourceLocation> sounds,
                               java.util.function.Consumer<ResourceLocation> onSelect) {
            super(x, y, width, height, Component.empty());
            this.minecraft = minecraft;
            this.listWidth = width;
            this.listHeight = height;
            this.x = x;
            this.y = y;
            this.sounds = sounds;
            this.onSelect = onSelect;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Enable scissor test for clipping
            graphics.enableScissor(x, y, x + listWidth, y + listHeight);

            int maxVisibleEntries = listHeight / ENTRY_HEIGHT;
            int startIndex = scrollOffset;
            int endIndex = Math.min(sounds.size(), startIndex + maxVisibleEntries);

            // Reset hovered index
            hoveredIndex = -1;

            // Render each visible sound entry
            for (int i = startIndex; i < endIndex; i++) {
                ResourceLocation sound = sounds.get(i);
                String soundName = sound.toString();

                int entryY = y + (i - startIndex) * ENTRY_HEIGHT;

                // Check if mouse is hovering this entry
                boolean isHovered = mouseX >= x && mouseX <= x + listWidth &&
                                    mouseY >= entryY && mouseY <= entryY + ENTRY_HEIGHT;

                if (isHovered) {
                    hoveredIndex = i;
                }

                // Render entry text with highlight if hovered
                int color = isHovered ? 0xFFD700 : 0xFFFFFF; // Gold when hovered, white otherwise
                graphics.drawString(minecraft.font, soundName, x + 3, entryY, color, false);
            }

            graphics.disableScissor();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && hoveredIndex >= 0 && hoveredIndex < sounds.size()) {
                // Left click - select the sound
                onSelect.accept(sounds.get(hoveredIndex));
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            // Check if mouse is within the widget bounds
            if (mouseX >= x && mouseX <= x + listWidth &&
                mouseY >= y && mouseY <= y + listHeight) {

                int maxScroll = Math.max(0, sounds.size() - (listHeight / ENTRY_HEIGHT));

                if (scrollY < 0) {
                    // Scroll down
                    scrollOffset = Math.min(scrollOffset + SCROLL_SPEED, maxScroll);
                } else if (scrollY > 0) {
                    // Scroll up
                    scrollOffset = Math.max(scrollOffset - SCROLL_SPEED, 0);
                }
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            narrationElementOutput.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                Component.literal("Sound list with " + sounds.size() + " recorded sounds"));
        }
    }
}
