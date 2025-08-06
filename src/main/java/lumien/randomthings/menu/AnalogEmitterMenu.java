package lumien.randomthings.menu;

import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.menu.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class AnalogEmitterMenu extends AbstractContainerMenu {
    private final AnalogEmitterBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public AnalogEmitterMenu(int containerId, Inventory playerInventory, AnalogEmitterBlockEntity blockEntity) {
        super(ModMenuTypes.ANALOG_EMITTER.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public AnalogEmitterMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, pos));
    }

    private static AnalogEmitterBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
            return analogEmitter;
        }
        throw new IllegalStateException("Block entity at " + pos + " is not an AnalogEmitterBlockEntity!");
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, this.blockEntity.getBlockState().getBlock());
    }

    public AnalogEmitterBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}