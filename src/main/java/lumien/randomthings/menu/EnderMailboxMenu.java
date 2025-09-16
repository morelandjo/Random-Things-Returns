package lumien.randomthings.menu;

import lumien.randomthings.blockentity.EnderMailboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EnderMailboxMenu extends AbstractContainerMenu {
    private final EnderMailboxBlockEntity mailbox;
    private final BlockPos pos;

    public EnderMailboxMenu(int id, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.ENDER_MAILBOX.get(), id);
        this.pos = pos;

        Level level = playerInventory.player.level();
        this.mailbox = (EnderMailboxBlockEntity) level.getBlockEntity(pos);

        if (mailbox != null) {
            // Add mailbox inventory slots (single row of 9 - output only)
            for (int i = 0; i < 9; i++) {
                this.addSlot(new SlotItemHandler(mailbox.getInventory(), i, 8 + i * 18, 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false; // Mailbox slots are read-only for received letters
                    }
                });
            }
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

            // If clicking on mailbox inventory
            if (index < 9) {
                // Try to move to player inventory
                if (!this.moveItemStackTo(slotStack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Can't move items from player inventory to mailbox
                return ItemStack.EMPTY;
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
        if (mailbox == null) {
            return false;
        }

        Level level = player.level();
        if (level.getBlockEntity(pos) != mailbox) {
            return false;
        }

        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}