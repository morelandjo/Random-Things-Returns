package lumien.randomthings.blockentity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks rain effects (rendering, particles, sound — via the client LevelRenderer mixin) within a
 * 5-chunk radius. Deactivated by a redstone signal. Instances register themselves in a static set
 * on both sides so {@link #shouldRain} works for client rendering and server logic alike.
 */
public class RainShieldBlockEntity extends BlockEntity {

    // 5 chunks = 80 blocks
    private static final int RAIN_SHIELD_RANGE = 80;

    public static final Set<RainShieldBlockEntity> shields = Collections.newSetFromMap(new WeakHashMap<>());

    private boolean active = true;

    public RainShieldBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.RAIN_SHIELD.get(), pos, blockState);

        synchronized (shields) {
            shields.add(this);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.active = tag.getBoolean("active");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("active", this.active);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("active", this.active);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        synchronized (shields) {
            shields.remove(this);
        }
    }

    /** False if an active rain shield protects this position (2D distance, Y ignored). */
    public static boolean shouldRain(Level level, BlockPos pos) {
        synchronized (shields) {
            for (RainShieldBlockEntity rainShield : shields) {
                // hasChunkAt filters block entities from unloaded chunks (no vanilla unload callback in 1.20.1)
                if (rainShield.level == level
                    && !rainShield.isRemoved()
                    && level.hasChunkAt(rainShield.getBlockPos())
                    && isInRange(rainShield.getBlockPos(), pos, RAIN_SHIELD_RANGE)) {
                    return !rainShield.active;
                }
            }
        }
        return true;
    }

    private static boolean isInRange(BlockPos shieldPos, BlockPos targetPos, int range) {
        double dx = shieldPos.getX() - targetPos.getX();
        double dz = shieldPos.getZ() - targetPos.getZ();
        return dx * dx + dz * dz < (double) range * range;
    }

    public void onBlockAdded(Level level, BlockPos pos, BlockState state) {
        this.active = !level.hasNeighborSignal(pos);
        setChanged();
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        boolean desiredState = !level.hasNeighborSignal(pos);
        if (desiredState != this.active) {
            this.active = desiredState;
            setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean isActive() {
        return active;
    }
}
