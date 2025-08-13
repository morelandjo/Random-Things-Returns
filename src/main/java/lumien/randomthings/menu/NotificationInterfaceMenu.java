package lumien.randomthings.menu;

import lumien.randomthings.blockentity.NotificationInterfaceBlockEntity;
import lumien.randomthings.menu.slot.GhostSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponentType;

public class NotificationInterfaceMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess levelAccess;
    private NotificationInterfaceBlockEntity blockEntity;

    public NotificationInterfaceMenu(int containerId, Inventory playerInventory, ContainerLevelAccess levelAccess) {
        super(ModMenuTypes.NOTIFICATION_INTERFACE.get(), containerId);
        this.levelAccess = levelAccess;
        
        // Try to get the block entity
        levelAccess.execute((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof NotificationInterfaceBlockEntity be) {
                this.blockEntity = be;
            }
        });

        // Ghost slot for the notification icon (only visual reference, no actual item storage)
        if (blockEntity != null) {
            this.addSlot(new GhostSlot(blockEntity.getItemHandler(), 0, 8, 31));
        }

        // Player inventory (3x9)
        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 64 + k * 18));
            }
        }

        // Player hotbar (1x9)
        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 122));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(levelAccess, player, blockEntity != null ? blockEntity.getBlockState().getBlock() : null);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            
            if (index == 0) {
                // Moving from ghost slot to player inventory
                if (!this.moveItemStackTo(slotStack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to ghost slot (only 1 item)
                if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        
        return itemStack;
    }

    public NotificationInterfaceBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean canDragTo(Slot slot) {
        // Allow dragging to all slots including ghost slot
        return super.canDragTo(slot);
    }
}