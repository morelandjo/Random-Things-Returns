package lumien.randomthings.menu;

import lumien.randomthings.item.ItemPortableSoundDampener;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.menu.slot.DisplayOnlySlot;
import lumien.randomthings.menu.slot.FilteredSlot;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Menu for the Portable Sound Dampener item.
 * Contains 9 slots for Sound Pattern items.
 */
public class PortableSoundDampenerMenu extends AbstractContainerMenu {
    private final Container dampenerInventory;
    private final ItemStack dampenerStack;
    private final InteractionHand hand;

    public PortableSoundDampenerMenu(int id, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.PORTABLE_SOUND_DAMPENER.get(), id);
        this.hand = hand;
        this.dampenerStack = playerInventory.player.getItemInHand(hand);

        if (!dampenerStack.isEmpty() && dampenerStack.is(ModItems.PORTABLE_SOUND_DAMPENER.get())) {
            // Create inventory backed by the item's data component
            this.dampenerInventory = new PortableSoundDampenerInventory(dampenerStack);

            // Sound Pattern slots (single row of 9)
            for (int col = 0; col < ItemPortableSoundDampener.INVENTORY_SIZE; col++) {
                addSlot(new FilteredSlot(dampenerInventory, col, 8 + col * 18, 18,
                    stack -> {
                        // Only accept filled Sound Pattern items
                        if (stack.isEmpty()) return true;
                        if (!stack.is(ModItems.SOUND_PATTERN.get())) return false;
                        return ItemSoundPattern.getSoundLocation(stack) != null;
                    }));
            }
        } else {
            this.dampenerInventory = null;
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            // If this is the slot holding the dampener, make it display-only
            if (playerInventory.getItem(col) == dampenerStack) {
                addSlot(new DisplayOnlySlot(playerInventory, col, 8 + col * 18, 109));
            } else {
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
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

            // From dampener inventory to player inventory
            if (index < ItemPortableSoundDampener.INVENTORY_SIZE) {
                if (!moveItemStackTo(slotStack, ItemPortableSoundDampener.INVENTORY_SIZE,
                                   ItemPortableSoundDampener.INVENTORY_SIZE + 36, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to dampener inventory
            else if (index >= ItemPortableSoundDampener.INVENTORY_SIZE &&
                     !moveItemStackTo(slotStack, 0, ItemPortableSoundDampener.INVENTORY_SIZE, false)) {
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
        return !dampenerStack.isEmpty() && player.getItemInHand(hand) == dampenerStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (dampenerInventory != null) {
            dampenerInventory.setChanged();
        }
    }

    /**
     * Container implementation that stores items in the Portable Sound Dampener's data component
     */
    private static class PortableSoundDampenerInventory extends ItemStackInventory {
        public PortableSoundDampenerInventory(ItemStack parentStack) {
            super(parentStack, ItemPortableSoundDampener.INVENTORY_SIZE, ModDataComponents.DAMPENER_INVENTORY.get());
        }
    }
}
