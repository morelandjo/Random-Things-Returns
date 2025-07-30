package lumien.randomthings.blockentity;

import lumien.randomthings.menu.AdvancedRedstoneTorchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedRedstoneTorchBlockEntity extends BlockEntity implements MenuProvider {
    private int signalStrengthRed = 15;
    private int signalStrengthGreen = 0;

    public AdvancedRedstoneTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ADVANCED_REDSTONE_TORCH.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("signalStrengthRed", signalStrengthRed);
        tag.putInt("signalStrengthGreen", signalStrengthGreen);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.signalStrengthRed = tag.getInt("signalStrengthRed");
        this.signalStrengthGreen = tag.getInt("signalStrengthGreen");
    }

    public int signalStrengthRed() {
        return signalStrengthRed;
    }

    public int signalStrengthGreen() {
        return signalStrengthGreen;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new AdvancedRedstoneTorchMenu(windowId, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("randomthings.block.advanced_redstone_torch");
    }

    public void setSignalStrengthGreen(int newValue) {
        this.signalStrengthGreen = newValue;
        this.setChanged();
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    public void setSignalStrengthRed(int newValue) {
        this.signalStrengthRed = newValue;
        this.setChanged();
        if (this.level != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }
}