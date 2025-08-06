package lumien.randomthings.blockentity;

import lumien.randomthings.menu.AnalogEmitterMenu;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;

public class AnalogEmitterBlockEntity extends BlockEntity implements MenuProvider {
    private int emitLevel = 1;
    private boolean powering = false;

    public AnalogEmitterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ANALOG_EMITTER.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("EmitLevel", this.emitLevel);
        tag.putBoolean("Powering", this.powering);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.emitLevel = tag.getInt("EmitLevel");
        this.powering = tag.getBoolean("Powering");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("EmitLevel", this.emitLevel);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.emitLevel = tag.getInt("EmitLevel");
    }

    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getEmitLevel() {
        return this.emitLevel;
    }
    
    public int getOutput() {
        if (this.powering) {
            return this.emitLevel;
        } else {
            return 0;
        }
    }
    
    public boolean isPowering() {
        return this.powering;
    }

    public void setEmitLevel(int level) {
        if (level >= 1 && level <= 15 && level != this.emitLevel) {
            this.emitLevel = level;
            this.setChanged();
            
            if (this.level != null && !this.level.isClientSide) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                this.neighborChanged();
            }
        }
    }

    public void neighborChanged() {
        if (this.level != null && !this.level.isClientSide) {
            // Check if input power state has changed (like original 1.12.2 code)
            boolean input = isInputSidePowered();
            
            if (input != this.powering) {
                this.powering = input;
                this.setChanged();
                // Update neighbors because our output state has changed
                this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
                this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
            }
        }
    }
    
    private boolean isInputSidePowered() {
        if (this.level == null) return false;
        
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof lumien.randomthings.block.AnalogEmitterBlock) {
            Direction facing = state.getValue(lumien.randomthings.block.AnalogEmitterBlock.FACING);
            BlockPos inputPos = this.worldPosition.relative(facing);
            int signal = this.level.getSignal(inputPos, facing.getOpposite());
            return signal > 0;
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.analog_emitter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AnalogEmitterMenu(containerId, playerInventory, this);
    }
}