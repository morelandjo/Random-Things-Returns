package lumien.randomthings.menu;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.menu.slot.DisplayOnlySlot;
import lumien.randomthings.menu.slot.FilteredSlot;
import lumien.randomthings.menu.slot.GhostSlot;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for editing the Redstone Remote configuration.
 * Top row: 9 Position Filter slots
 * Bottom row: 9 Ghost slots for display icons
 */
public class RedstoneRemoteEditMenu extends AbstractContainerMenu {
    private final Container remoteInventory;
    private final ItemStack remoteStack;
    private final InteractionHand hand;

    public RedstoneRemoteEditMenu(int id, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.REDSTONE_REMOTE_EDIT.get(), id);
        this.hand = hand;
        this.remoteStack = playerInventory.player.getItemInHand(hand);

        if (!remoteStack.isEmpty() && remoteStack.is(ModItems.REDSTONE_REMOTE.get())) {
            this.remoteInventory = new ItemStackInventory(remoteStack, 18);

            // Position Filter slots (top row)
            for (int col = 0; col < 9; col++) {
                addSlot(new FilteredSlot(remoteInventory, col, 8 + col * 18, 18,
                    stack -> stack.is(ModItems.POSITION_FILTER.get())));
            }

            // Ghost slots for display icons (bottom row)
            for (int col = 0; col < 9; col++) {
                addSlot(new GhostSlot(remoteInventory, col + 9, 8 + col * 18, 36));
            }
        } else {
            this.remoteInventory = null;
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 68 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            // If this is the slot holding the remote, make it display-only
            if (playerInventory.getItem(col) == remoteStack) {
                addSlot(new DisplayOnlySlot(playerInventory, col, 8 + col * 18, 126));
            } else {
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 126));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            // From remote inventory to player inventory
            if (index < 9) {
                if (!moveItemStackTo(slotStack, 18, 54, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // From player inventory to remote inventory
            else if (index >= 18 && !moveItemStackTo(slotStack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return !remoteStack.isEmpty() && player.getItemInHand(hand) == remoteStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (remoteInventory != null) {
            remoteInventory.setChanged();
        }
    }
}
