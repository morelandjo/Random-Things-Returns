package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.block.AnalogEmitterBlock;
import lumien.randomthings.menu.AnalogEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnalogEmitterBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private int emitLevel = 1;
    private boolean powering = false;

    public AnalogEmitterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ANALOG_EMITTER.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("EmitLevel", this.emitLevel);
        tag.putBoolean("Powering", this.powering);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.emitLevel = tag.getInt("EmitLevel");
        this.powering = tag.getBoolean("Powering");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("EmitLevel", this.emitLevel);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getEmitLevel() {
        return this.emitLevel;
    }

    public int getOutput() {
        return this.powering ? this.emitLevel : 0;
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
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        boolean input = isInputSidePowered();
        if (input != this.powering) {
            this.powering = input;
            this.setChanged();
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    private boolean isInputSidePowered() {
        if (this.level == null) {
            return false;
        }
        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof AnalogEmitterBlock) {
            Direction facing = state.getValue(AnalogEmitterBlock.FACING);
            BlockPos inputPos = this.worldPosition.relative(facing);
            return this.level.getSignal(inputPos, facing.getOpposite()) > 0;
        }
        return false;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.analog_emitter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AnalogEmitterMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
