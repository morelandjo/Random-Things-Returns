package lumien.randomthings.menu.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A ghost slot that displays an item but doesn't actually hold it.
 * Items can be placed in but not removed. The slot shows a single copy of the item.
 */
public class GhostSlot extends Slot {
    public GhostSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        // When player tries to pick up (remove) the item
        // Clear the slot instead
        this.set(ItemStack.EMPTY);
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // When player tries to place an item
        // Store a single copy of it
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.set(copy);
        return false; // Return false so the original stack doesn't get consumed
    }

    @Override
    public ItemStack remove(int amount) {
        // Clear the slot when trying to remove
        this.set(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
