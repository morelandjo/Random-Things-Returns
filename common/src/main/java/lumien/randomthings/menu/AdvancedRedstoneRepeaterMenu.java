package lumien.randomthings.menu;

import lumien.randomthings.blockentity.AdvancedRedstoneRepeaterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AdvancedRedstoneRepeaterMenu extends AbstractContainerMenu {
    private final AdvancedRedstoneRepeaterBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public AdvancedRedstoneRepeaterMenu(int windowId, Inventory playerInventory, AdvancedRedstoneRepeaterBlockEntity blockEntity) {
        super(ModMenuTypes.ADVANCED_REDSTONE_REPEATER.get(), windowId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public AdvancedRedstoneRepeaterMenu(int windowId, Inventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    public AdvancedRedstoneRepeaterMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(windowId, playerInventory, buf.readBlockPos());
    }

    private static AdvancedRedstoneRepeaterBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        if (be instanceof AdvancedRedstoneRepeaterBlockEntity arr) {
            return arr;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an AdvancedRedstoneRepeaterBlockEntity!");
    }

    public AdvancedRedstoneRepeaterBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public int getTurnOffDelay() {
        return this.blockEntity.getTurnOffDelay();
    }

    public int getTurnOnDelay() {
        return this.blockEntity.getTurnOnDelay();
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
