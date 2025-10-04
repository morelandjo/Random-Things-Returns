package lumien.randomthings.menu;

import lumien.randomthings.item.ItemFilterItem;
import lumien.randomthings.menu.slots.GhostSlot;
import lumien.randomthings.util.ItemFilterData;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemFilterMenu extends AbstractContainerMenu {
    private final Player player;
    private final ItemStack filterStack;
    private final SimpleContainer filterInventory;
    private final ContainerData data;

    // Data slots for syncing filter state to client
    private static final int METADATA_SLOT = 0;
    private static final int NBT_SLOT = 1;
    private static final int TAGS_SLOT = 2;
    private static final int LISTTYPE_SLOT = 3;

    public ItemFilterMenu(int containerId, Inventory playerInventory, ItemStack filterStack) {
        super(ModMenuTypes.ITEM_FILTER.get(), containerId);
        this.player = playerInventory.player;
        this.filterStack = filterStack;
        this.filterInventory = new SimpleContainer(9);
        this.data = new SimpleContainerData(4); // 4 slots for the 4 filter settings

        // Load filter data from itemstack into temporary inventory
        ItemFilterData filterData = ItemFilterItem.getFilterData(filterStack);
        List<ItemStack> filterItems = filterData.filterItems();
        for (int i = 0; i < 9; i++) {
            if (i < filterItems.size()) {
                filterInventory.setItem(i, filterItems.get(i).copy());
            }
        }

        // Initialize data slots
        data.set(METADATA_SLOT, filterData.checkMetadata() ? 1 : 0);
        data.set(NBT_SLOT, filterData.checkNBT() ? 1 : 0);
        data.set(TAGS_SLOT, filterData.checkTags() ? 1 : 0);
        data.set(LISTTYPE_SLOT, filterData.listType());

        this.addDataSlots(this.data);

        // Add ghost slots for filter items (9 slots in a row)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new GhostSlot(filterInventory, i, 8 + i * 18, 18));
        }

        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!player.level().isClientSide) {
            ItemFilterData currentData = ItemFilterItem.getFilterData(filterStack);
            ItemFilterData newData = currentData;

            switch (buttonId) {
                case 0: // Toggle metadata
                    newData = currentData.toggleMetadata();
                    data.set(METADATA_SLOT, newData.checkMetadata() ? 1 : 0);
                    break;
                case 1: // Toggle tags
                    newData = currentData.toggleTags();
                    data.set(TAGS_SLOT, newData.checkTags() ? 1 : 0);
                    break;
                case 2: // Toggle NBT
                    newData = currentData.toggleNBT();
                    data.set(NBT_SLOT, newData.checkNBT() ? 1 : 0);
                    break;
                case 3: // Toggle list type
                    newData = currentData.toggleListType();
                    data.set(LISTTYPE_SLOT, newData.listType());
                    break;
            }

            ItemFilterItem.setFilterData(filterStack, newData);
            return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // Save filter inventory back to itemstack
        if (!player.level().isClientSide) {
            ItemFilterData currentData = ItemFilterItem.getFilterData(filterStack);
            List<ItemStack> filterItems = new ArrayList<>();

            for (int i = 0; i < 9; i++) {
                filterItems.add(filterInventory.getItem(i).copy());
            }

            ItemFilterData newData = currentData.withFilterItems(filterItems);
            ItemFilterItem.setFilterData(filterStack, newData);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();

            if (index < 9) {
                // From ghost slots - don't do anything
                return ItemStack.EMPTY;
            } else {
                // From player inventory - try to place in first empty ghost slot
                for (int i = 0; i < 9; i++) {
                    Slot ghostSlot = this.slots.get(i);
                    if (!ghostSlot.hasItem()) {
                        ItemStack copy = slotStack.copy();
                        copy.setCount(1);
                        ghostSlot.set(copy);
                        return ItemStack.EMPTY;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem() instanceof ItemFilterItem && !held.isEmpty() && held == filterStack;
    }

    // Getters for client-side UI
    public boolean getCheckMetadata() {
        return data.get(METADATA_SLOT) != 0;
    }

    public boolean getCheckNBT() {
        return data.get(NBT_SLOT) != 0;
    }

    public boolean getCheckTags() {
        return data.get(TAGS_SLOT) != 0;
    }

    public int getListType() {
        return data.get(LISTTYPE_SLOT);
    }

    public ItemStack getFilterStack() {
        return filterStack;
    }
}
