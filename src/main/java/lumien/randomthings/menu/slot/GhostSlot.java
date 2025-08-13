package lumien.randomthings.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class GhostSlot extends Slot {
    private final IItemHandler itemHandler;
    private final int slotIndex;

    // Dummy container that delegates to IItemHandler
    private static class GhostContainer implements Container {
        private final IItemHandler handler;
        
        public GhostContainer(IItemHandler handler) {
            this.handler = handler;
        }
        
        @Override
        public int getContainerSize() {
            return handler.getSlots();
        }
        
        @Override
        public boolean isEmpty() {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public ItemStack getItem(int slot) {
            return handler.getStackInSlot(slot);
        }
        
        @Override
        public ItemStack removeItem(int slot, int amount) {
            return ItemStack.EMPTY; // Ghost slots don't remove
        }
        
        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }
        
        @Override
        public void setItem(int slot, ItemStack stack) {
            if (handler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(slot, stack);
            }
        }
        
        @Override
        public void setChanged() {}
        
        @Override
        public boolean stillValid(Player player) {
            return true;
        }
        
        @Override
        public void clearContent() {}
    }

    public GhostSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(new GhostContainer(itemHandler), index, x, y);
        this.itemHandler = itemHandler;
        this.slotIndex = index;
    }

    @Override
    public ItemStack getItem() {
        return this.itemHandler.getStackInSlot(this.slotIndex);
    }

    @Override
    public void set(ItemStack stack) {
        if (itemHandler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
            modifiable.setStackInSlot(this.slotIndex, stack);
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
        // Allow removing items from the ghost slot
        if (itemHandler instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable) {
            ItemStack existing = modifiable.getStackInSlot(this.slotIndex);
            if (!existing.isEmpty()) {
                int toRemove = Math.min(amount, existing.getCount());
                ItemStack result = existing.copy();
                result.setCount(toRemove);
                
                ItemStack remaining = existing.copy();
                remaining.shrink(toRemove);
                modifiable.setStackInSlot(this.slotIndex, remaining);
                
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        // Allow picking up from ghost slots
        return true;
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