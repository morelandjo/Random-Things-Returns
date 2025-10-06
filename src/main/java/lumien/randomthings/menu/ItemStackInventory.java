package lumien.randomthings.menu;

import lumien.randomthings.item.ModDataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * A Container implementation that stores items in an ItemStack's data components
 */
public class ItemStackInventory implements Container {
    private final ItemStack parentStack;
    private final int size;
    private NonNullList<ItemStack> items;

    public ItemStackInventory(ItemStack parentStack, int size) {
        this.parentStack = parentStack;
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);

        // Load from data component
        ItemContainerContents contents = parentStack.get(ModDataComponents.REDSTONE_REMOTE_INVENTORY.get());
        if (contents != null) {
            contents.copyInto(this.items);
        }
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        // Save to data component
        parentStack.set(ModDataComponents.REDSTONE_REMOTE_INVENTORY.get(),
            ItemContainerContents.fromItems(items));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }
}
