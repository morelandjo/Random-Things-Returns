package lumien.randomthings.menu;

import lumien.randomthings.item.ItemPortableSoundDampener;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.menu.slots.FilteredSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Nine Sound Pattern slots stored on the portable dampener item itself. */
public class PortableSoundDampenerMenu extends AbstractContainerMenu {
    private final ItemStack dampenerStack;
    private final SimpleContainer patterns;

    public PortableSoundDampenerMenu(int containerId, Inventory playerInventory, ItemStack dampenerStack) {
        super(ModMenuTypes.PORTABLE_SOUND_DAMPENER.get(), containerId);
        this.dampenerStack = dampenerStack;
        this.patterns = ItemPortableSoundDampener.loadInventory(dampenerStack);

        for (int col = 0; col < ItemPortableSoundDampener.SIZE; col++) {
            this.addSlot(new FilteredSlot(patterns, col, 8 + col * 18, 18,
                stack -> stack.getItem() instanceof ItemSoundPattern));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            int slotIndex = col;
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109) {
                @Override
                public boolean mayPickup(Player player) {
                    // Don't let the player move the open dampener out from under the menu.
                    return playerInventory.getItem(slotIndex) != dampenerStack;
                }
            });
        }
    }

    public PortableSoundDampenerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    @Override
    public void slotsChanged(net.minecraft.world.Container container) {
        super.slotsChanged(container);
        ItemPortableSoundDampener.saveInventory(dampenerStack, patterns);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ItemPortableSoundDampener.saveInventory(dampenerStack, patterns);
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().contains(dampenerStack) || player.getOffhandItem() == dampenerStack;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < ItemPortableSoundDampener.SIZE) {
                if (!this.moveItemStackTo(stack, ItemPortableSoundDampener.SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!(stack.getItem() instanceof ItemSoundPattern)
                || !this.moveItemStackTo(stack, 0, ItemPortableSoundDampener.SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            ItemPortableSoundDampener.saveInventory(dampenerStack, patterns);
        }
        return result;
    }
}
