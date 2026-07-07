package lumien.randomthings.menu;

import lumien.randomthings.item.ItemFilterItem;
import lumien.randomthings.menu.slots.GhostSlot;
import lumien.randomthings.util.ItemFilterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
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
    private final ItemStack filterStack;
    private final SimpleContainer filterInventory;
    private final ContainerData data;

    private static final int METADATA_SLOT = 0;
    private static final int NBT_SLOT = 1;
    private static final int TAGS_SLOT = 2;
    private static final int LISTTYPE_SLOT = 3;

    /** Client-side constructor used by the Architectury extended menu factory. */
    public ItemFilterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.getItemInHand(buf.readEnum(InteractionHand.class)));
    }

    public ItemFilterMenu(int containerId, Inventory playerInventory, ItemStack filterStack) {
        super(ModMenuTypes.ITEM_FILTER.get(), containerId);
        this.filterStack = filterStack;
        this.filterInventory = new SimpleContainer(9);
        this.data = new SimpleContainerData(4);

        ItemFilterData filterData = ItemFilterItem.getFilterData(filterStack);
        List<ItemStack> filterItems = filterData.filterItems();
        for (int i = 0; i < 9; i++) {
            if (i < filterItems.size()) {
                filterInventory.setItem(i, filterItems.get(i).copy());
            }
        }

        data.set(METADATA_SLOT, filterData.checkMetadata() ? 1 : 0);
        data.set(NBT_SLOT, filterData.checkNBT() ? 1 : 0);
        data.set(TAGS_SLOT, filterData.checkTags() ? 1 : 0);
        data.set(LISTTYPE_SLOT, filterData.listType());

        this.addDataSlots(this.data);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new GhostSlot(filterInventory, i, 8 + i * 18, 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!player.level().isClientSide) {
            ItemFilterData currentData = ItemFilterItem.getFilterData(filterStack);
            ItemFilterData newData = switch (buttonId) {
                case 0 -> currentData.toggleMetadata();
                case 1 -> currentData.toggleTags();
                case 2 -> currentData.toggleNBT();
                case 3 -> currentData.toggleListType();
                default -> currentData;
            };
            data.set(METADATA_SLOT, newData.checkMetadata() ? 1 : 0);
            data.set(NBT_SLOT, newData.checkNBT() ? 1 : 0);
            data.set(TAGS_SLOT, newData.checkTags() ? 1 : 0);
            data.set(LISTTYPE_SLOT, newData.listType());
            ItemFilterItem.setFilterData(filterStack, newData);
            return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemFilterData currentData = ItemFilterItem.getFilterData(filterStack);
            List<ItemStack> filterItems = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                filterItems.add(filterInventory.getItem(i).copy());
            }
            ItemFilterItem.setFilterData(filterStack, currentData.withFilterItems(filterItems));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            if (index < 9) {
                return ItemStack.EMPTY; // ghost slots
            }
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
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack held = player.getMainHandItem();
        return !held.isEmpty() && held.getItem() instanceof ItemFilterItem && held == filterStack;
    }

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
