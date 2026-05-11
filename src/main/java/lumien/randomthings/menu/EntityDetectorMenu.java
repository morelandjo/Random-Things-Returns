package lumien.randomthings.menu;

import lumien.randomthings.blockentity.EntityDetectorBlockEntity;
import lumien.randomthings.item.EntityFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class EntityDetectorMenu extends AbstractContainerMenu {
    private final EntityDetectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public EntityDetectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.ENTITY_DETECTOR.get(), containerId);
        this.blockEntity = getBlockEntity(playerInventory, pos);
        this.access = ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());

        // Filter item slot — at (152, 56) within a 176-wide GUI (right side).
        this.addSlot(new SlotItemHandler(this.blockEntity.getFilterSlot(), 0, 152, 56) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof EntityFilterItem;
            }
        });

        addPlayerInventory(playerInventory);
        addDataSlots(this.blockEntity.containerData);
    }

    private static EntityDetectorBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof EntityDetectorBlockEntity detector) {
            return detector;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an EntityDetectorBlockEntity!");
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public EntityDetectorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        // Slot 0 is the filter slot; everything else is the player inventory.
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (stack.getItem() instanceof EntityFilterItem) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }
}
