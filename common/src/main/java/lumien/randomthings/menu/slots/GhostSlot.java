package lumien.randomthings.menu.slots;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A ghost slot that displays an item but doesn't actually hold it. Items can be placed (a single
 * copy is stored) but not removed by taking. Used for filter configuration UIs.
 */
public class GhostSlot extends Slot {
    public GhostSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        this.set(ItemStack.EMPTY);
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        this.set(copy);
        return false; // don't consume the original stack
    }

    @Override
    public ItemStack remove(int amount) {
        this.set(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
