package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RedstoneObserverBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos target;
    private final int[] weakPower = new int[6];
    private final int[] strongPower = new int[6];

    public RedstoneObserverBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.REDSTONE_OBSERVER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedstoneObserverBlockEntity be) {
        be.poll(level, pos);
    }

    private void poll(Level level, BlockPos pos) {
        boolean changed = false;

        if (this.target == null) {
            for (int i = 0; i < 6; i++) {
                if (this.weakPower[i] != 0 || this.strongPower[i] != 0) {
                    this.weakPower[i] = 0;
                    this.strongPower[i] = 0;
                    changed = true;
                }
            }
        } else {
            BlockState targetState = level.getBlockState(this.target);
            for (Direction dir : Direction.values()) {
                int idx = dir.ordinal();
                int newWeak = targetState.getSignal(level, this.target, dir);
                int newStrong = targetState.getDirectSignal(level, this.target, dir);
                if (this.weakPower[idx] != newWeak || this.strongPower[idx] != newStrong) {
                    this.weakPower[idx] = newWeak;
                    this.strongPower[idx] = newStrong;
                    changed = true;
                }
            }
        }

        if (changed) {
            this.setChanged();
            level.updateNeighborsAt(pos, this.getBlockState().getBlock());
        }
    }

    public int getWeakPower(Direction direction) {
        return this.weakPower[direction.ordinal()];
    }

    public int getStrongPower(Direction direction) {
        return this.strongPower[direction.ordinal()];
    }

    @Nullable
    public BlockPos getTarget() {
        return this.target;
    }

    public void setTarget(@Nullable BlockPos target) {
        this.target = target;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.target != null) {
            tag.putInt("TargetX", this.target.getX());
            tag.putInt("TargetY", this.target.getY());
            tag.putInt("TargetZ", this.target.getZ());
        }
        tag.putIntArray("WeakPower", java.util.Arrays.copyOf(this.weakPower, 6));
        tag.putIntArray("StrongPower", java.util.Arrays.copyOf(this.strongPower, 6));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("TargetX")) {
            this.target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        } else {
            this.target = null;
        }
        int[] weak = tag.getIntArray("WeakPower");
        int[] strong = tag.getIntArray("StrongPower");
        if (weak.length == 6) System.arraycopy(weak, 0, this.weakPower, 0, 6);
        if (strong.length == 6) System.arraycopy(strong, 0, this.strongPower, 0, 6);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (this.target != null) {
            tag.putInt("TargetX", this.target.getX());
            tag.putInt("TargetY", this.target.getY());
            tag.putInt("TargetZ", this.target.getZ());
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
