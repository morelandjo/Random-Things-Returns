package lumien.randomthings.menu;

import lumien.randomthings.blockentity.OnlineDetectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class OnlineDetectorMenu extends AbstractContainerMenu {
    private final OnlineDetectorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public OnlineDetectorMenu(int containerId, Inventory playerInventory, OnlineDetectorBlockEntity blockEntity) {
        super(null /* ModMenuTypes.ONLINE_DETECTOR.get() */, containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public OnlineDetectorMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static OnlineDetectorBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof OnlineDetectorBlockEntity onlineDetector) {
            return onlineDetector;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an OnlineDetectorBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public OnlineDetectorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}