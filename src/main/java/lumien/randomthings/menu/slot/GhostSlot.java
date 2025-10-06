package lumien.randomthings.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A slot that acts as a "ghost" slot - items can be placed and removed,
 * but the operations work directly with copies without consuming items from player inventory
 */
public class GhostSlot extends Slot {

    public GhostSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack stack = getItem();
        if (!stack.isEmpty()) {
            // Return a copy without modifying the original
            return stack.copyWithCount(Math.min(amount, stack.getCount()));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }
}
