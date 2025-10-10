package lumien.randomthings.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A slot that prevents items from being placed into it (output-only)
 */
public class OutputOnlySlot extends Slot {

    public OutputOnlySlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false; // Cannot place items in this slot
    }
}
