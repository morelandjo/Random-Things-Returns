package lumien.randomthings.menu;

import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.menu.slot.DisplayOnlySlot;
import lumien.randomthings.menu.slot.FilteredSlot;
import lumien.randomthings.menu.slot.OutputOnlySlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SoundRecorderMenu extends AbstractContainerMenu {
    private final Container tempInventory;
    private final Level level;
    private final InteractionHand hand;
    private final ItemStack recorderStack;

    private Slot patternInputSlot;
    private Slot patternOutputSlot;

    public SoundRecorderMenu(int id, Inventory playerInventory) {
        super(ModMenuTypes.SOUND_RECORDER.get(), id);
        this.level = playerInventory.player.level();
        this.hand = InteractionHand.MAIN_HAND; // Assume main hand for now
        this.recorderStack = playerInventory.player.getItemInHand(hand);

        // Create temporary inventory for 2 slots: input and output for Sound Patterns
        this.tempInventory = new SimpleContainer(2);

        // Input slot for empty Sound Patterns (slot 0, position 65, 73)
        this.patternInputSlot = addSlot(new FilteredSlot(tempInventory, 0, 65, 73,
            stack -> stack.isEmpty() || stack.is(ModItems.SOUND_PATTERN.get())));

        // Output slot for filled Sound Patterns (slot 1, position 108, 73)
        this.patternOutputSlot = addSlot(new OutputOnlySlot(tempInventory, 1, 108, 73));

        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 15 + col * 18, 105 + row * 18));
            }
        }

        // Player hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            // If this is the slot holding the recorder, make it display-only
            if (playerInventory.getItem(col) == recorderStack) {
                addSlot(new DisplayOnlySlot(playerInventory, col, 15 + col * 18, 163));
            } else {
                addSlot(new Slot(playerInventory, col, 15 + col * 18, 163));
            }
        }
    }

    /**
     * Called when a sound is selected in the GUI to create a filled Sound Pattern
     */
    public void outputSound(ResourceLocation selectedSound) {
        if (patternInputSlot.hasItem()) {
            ItemStack inputPattern = patternInputSlot.getItem();
            ItemStack outputPattern = new ItemStack(ModItems.SOUND_PATTERN.get());

            // Set the sound on the output pattern
            ItemSoundPattern.setSoundLocation(outputPattern, selectedSound);

            // Try to add to output slot or merge
            ItemStack currentOutput = patternOutputSlot.getItem();
            if (currentOutput.isEmpty()) {
                // Output slot is empty, just place the pattern
                patternOutputSlot.set(outputPattern);
                inputPattern.shrink(1);
            } else if (ItemStack.isSameItemSameComponents(currentOutput, outputPattern) &&
                       currentOutput.getCount() < 64) {
                // Output slot has the same pattern and can stack
                currentOutput.grow(1);
                inputPattern.shrink(1);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From temp inventory to player inventory
            if (index < 2) {
                if (!moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to pattern input slot
            else if (index >= 2 && !moveItemStackTo(slotStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return !recorderStack.isEmpty() && player.getItemInHand(hand) == recorderStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Drop any items left in the temporary inventory
        if (!level.isClientSide) {
            for (int i = 0; i < tempInventory.getContainerSize(); i++) {
                ItemStack stack = tempInventory.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    /**
     * Get the Sound Recorder stack being used
     */
    public ItemStack getRecorderStack() {
        return recorderStack;
    }
}
