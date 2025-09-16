package lumien.randomthings.menu;

import lumien.randomthings.item.EnderLetterItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EnderLetterMenu extends AbstractContainerMenu {
    private ItemStack letterStack;
    private final ItemStackHandler letterInventory;

    public EnderLetterMenu(int id, Inventory playerInventory, ItemStack letterStack) {
        super(ModMenuTypes.ENDER_LETTER.get(), id);
        this.letterStack = letterStack;
        this.letterInventory = EnderLetterItem.getInventory(letterStack);

        // Add letter inventory slots (single row of 9)
        for (int i = 0; i < 9; i++) {
            this.addSlot(new SlotItemHandler(letterInventory, i, 8 + i * 18, 18));
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
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // If clicking on letter inventory
            if (index < 9) {
                // Try to move to player inventory
                if (!this.moveItemStackTo(slotStack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // If clicking on player inventory, try to move to letter inventory
                if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Check if player still has the letter in their inventory
        ItemStack currentItem = player.getMainHandItem();
        if (currentItem.getItem() instanceof EnderLetterItem) {
            // Update our reference to the current letter stack
            this.letterStack = currentItem;
            return true;
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Save the inventory back to the letter
        EnderLetterItem.setInventory(letterStack, letterInventory);
    }

    public String getReceiver() {
        return EnderLetterItem.getReceiver(letterStack);
    }

    public void setReceiver(String receiver) {
        EnderLetterItem.setReceiver(letterStack, receiver);
    }

    public boolean isSigned() {
        return EnderLetterItem.isSigned(letterStack);
    }
}