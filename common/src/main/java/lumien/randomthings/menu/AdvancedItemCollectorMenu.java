package lumien.randomthings.menu;

import lumien.randomthings.blockentity.AdvancedItemCollectorBlockEntity;
import lumien.randomthings.item.ItemFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedItemCollectorMenu extends AbstractContainerMenu {
    private final AdvancedItemCollectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    /** Client-side constructor used by the Architectury extended menu factory. */
    public AdvancedItemCollectorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    public AdvancedItemCollectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.ADVANCED_ITEM_COLLECTOR.get(), containerId);
        this.blockEntity = resolve(playerInventory, pos);
        this.access = ContainerLevelAccess.create(this.blockEntity.getLevel(), this.blockEntity.getBlockPos());

        this.addSlot(new Slot(this.blockEntity.getFilterSlot(), 0, 152, 56) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemFilterItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        addPlayerInventory(playerInventory);
        addDataSlots(this.blockEntity.containerData);
    }

    private static AdvancedItemCollectorBlockEntity resolve(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof AdvancedItemCollectorBlockEntity collector) return collector;
        throw new IllegalStateException("Block entity at " + pos + " is not an AdvancedItemCollectorBlockEntity");
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

    public AdvancedItemCollectorBlockEntity getBlockEntity() {
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
        if (index == 0) {
            if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (stack.getItem() instanceof ItemFilterItem) {
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
