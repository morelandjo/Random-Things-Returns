package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Basic Redstone Interface — a "transmitter" that mirrors the redstone signals it sees from its 6
 * neighbors into the 6 faces of its linked target interface. Pairing is one-way per link (A→B);
 * for two-way relay the player creates two pairs, A→B and B→A.
 */
public class BasicRedstoneInterfaceBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos target;

    /** Per-face emit values driven by a remote interface that is targeting THIS block. */
    private final int[] emittedWeak = new int[6];
    private final int[] emittedStrong = new int[6];

    /** Cached last-read inputs from this interface's own neighbors. Used to detect change. */
    private final int[] lastLocalWeak = new int[6];
    private final int[] lastLocalStrong = new int[6];

    private boolean primed = false;

    public BasicRedstoneInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BASIC_REDSTONE_INTERFACE.get(), pos, blockState);
    }

    @Nullable
    public BlockPos getTarget() {
        return this.target;
    }

    /** Called from RedstoneToolItem when the second click lands. */
    public void setTarget(@Nullable BlockPos newTarget) {
        BlockPos oldTarget = this.target;
        this.target = newTarget;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            // Stop emitting at the previous target.
            if (oldTarget != null) {
                pushEmpty(oldTarget);
            }
            // Re-read local state and push to the new target immediately.
            primed = false;
            onLocalRedstoneChanged();
        }
    }

    public int getEmittedWeak(Direction direction) {
        return this.emittedWeak[direction.ordinal()];
    }

    public int getEmittedStrong(Direction direction) {
        return this.emittedStrong[direction.ordinal()];
    }

    /**
     * Read our 6 neighbors' redstone outputs. If they differ from the last reading, push the new
     * values to our linked target interface. The target then emits them on its own 6 faces.
     */
    public void onLocalRedstoneChanged() {
        if (this.level == null || this.level.isClientSide) return;
        int[] newWeak = new int[6];
        int[] newStrong = new int[6];
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = this.worldPosition.relative(dir);
            BlockState neighborState = this.level.getBlockState(neighborPos);
            newWeak[dir.ordinal()] = neighborState.getSignal(this.level, neighborPos, dir);
            newStrong[dir.ordinal()] = neighborState.getDirectSignal(this.level, neighborPos, dir);
        }

        boolean changed = !primed
            || !arraysEqual(newWeak, this.lastLocalWeak)
            || !arraysEqual(newStrong, this.lastLocalStrong);

        if (!changed) return;

        System.arraycopy(newWeak, 0, this.lastLocalWeak, 0, 6);
        System.arraycopy(newStrong, 0, this.lastLocalStrong, 0, 6);
        this.primed = true;
        this.setChanged();

        if (this.target != null) {
            BlockEntity be = this.level.getBlockEntity(this.target);
            if (be instanceof BasicRedstoneInterfaceBlockEntity peer) {
                peer.acceptEmitFromPeer(newWeak, newStrong);
            }
        }
    }

    private void acceptEmitFromPeer(int[] weak, int[] strong) {
        if (this.level == null || this.level.isClientSide) return;
        if (arraysEqual(weak, this.emittedWeak) && arraysEqual(strong, this.emittedStrong)) return;
        System.arraycopy(weak, 0, this.emittedWeak, 0, 6);
        System.arraycopy(strong, 0, this.emittedStrong, 0, 6);
        this.setChanged();
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
    }

    private void pushEmpty(BlockPos peerPos) {
        if (this.level == null) return;
        BlockEntity be = this.level.getBlockEntity(peerPos);
        if (be instanceof BasicRedstoneInterfaceBlockEntity peer) {
            peer.acceptEmitFromPeer(new int[6], new int[6]);
        }
    }

    public void onBlockRemoved() {
        if (this.target != null) {
            pushEmpty(this.target);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BasicRedstoneInterfaceBlockEntity be) {
        // First tick after load: prime by reading local signals and pushing to the target.
        if (!be.primed) {
            be.onLocalRedstoneChanged();
        }
    }

    private static boolean arraysEqual(int[] a, int[] b) {
        for (int i = 0; i < 6; i++) if (a[i] != b[i]) return false;
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (target != null) {
            tag.putInt("TargetX", target.getX());
            tag.putInt("TargetY", target.getY());
            tag.putInt("TargetZ", target.getZ());
        }
        tag.putIntArray("EmittedWeak", java.util.Arrays.copyOf(emittedWeak, 6));
        tag.putIntArray("EmittedStrong", java.util.Arrays.copyOf(emittedStrong, 6));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("TargetX")) {
            this.target = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        }
        int[] w = tag.getIntArray("EmittedWeak");
        int[] s = tag.getIntArray("EmittedStrong");
        if (w.length == 6) System.arraycopy(w, 0, emittedWeak, 0, 6);
        if (s.length == 6) System.arraycopy(s, 0, emittedStrong, 0, 6);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (target != null) {
            tag.putInt("TargetX", target.getX());
            tag.putInt("TargetY", target.getY());
            tag.putInt("TargetZ", target.getZ());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
