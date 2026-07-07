package lumien.randomthings.menu;

import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedRedstoneTorchMenu extends AbstractContainerMenu {
    private final AdvancedRedstoneTorchBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public AdvancedRedstoneTorchMenu(int windowId, Inventory playerInventory, AdvancedRedstoneTorchBlockEntity blockEntity) {
        super(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), windowId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public AdvancedRedstoneTorchMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    public AdvancedRedstoneTorchMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, buf.readBlockPos());
    }

    private static AdvancedRedstoneTorchBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof AdvancedRedstoneTorchBlockEntity art) {
            return art;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an AdvancedRedstoneTorchBlockEntity!");
    }

    public AdvancedRedstoneTorchBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getSignalStrengthGreen() {
        return this.blockEntity.signalStrengthGreen();
    }

    public int getSignalStrengthRed() {
        return this.blockEntity.signalStrengthRed();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
