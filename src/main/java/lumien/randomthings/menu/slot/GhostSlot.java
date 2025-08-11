package lumien.randomthings.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class GhostSlot extends Slot {
    private final IItemHandler itemHandler;
    private final int index;

    public GhostSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(null, index, x, y);
        this.itemHandler = itemHandler;
        this.index = index;
    }

    @Override
    public ItemStack getItem() {
        return this.itemHandler.getStackInSlot(this.index);
    }

    @Override
    public void set(ItemStack stack) {
        if (itemHandler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(this.index, stack);
        }
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
        // Do nothing for ghost slots
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(this.getMaxStackSize(), stack.getMaxStackSize());
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack remove(int amount) {
        // Ghost slots don't actually remove items
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        // Can't pick up from ghost slots
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void setChanged() {
        // Notify the container that the slot changed
    }
}