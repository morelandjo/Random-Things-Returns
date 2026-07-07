package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Peace candles register themselves in a global set; {@code RTEvents} consults {@link #isPeacefulChunk}
 * from Architectury's {@code LIVING_CHECK_SPAWN} event to suppress monster spawns nearby.
 */
public class PeaceCandleBlockEntity extends BlockEntity {
    public static final Set<PeaceCandleBlockEntity> CANDLES = Collections.newSetFromMap(new WeakHashMap<>());

    public PeaceCandleBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.PEACE_CANDLE.get(), pos, blockState);
        synchronized (CANDLES) {
            CANDLES.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        synchronized (CANDLES) {
            CANDLES.remove(this);
        }
    }

    /** True if the chunk lies within 3 chunks of an active peace candle. */
    public static boolean isPeacefulChunk(int chunkX, int chunkZ) {
        synchronized (CANDLES) {
            for (PeaceCandleBlockEntity candle : CANDLES) {
                if (!candle.isRemoved() && candle.getLevel() != null) {
                    int candleChunkX = candle.getBlockPos().getX() >> 4;
                    int candleChunkZ = candle.getBlockPos().getZ() >> 4;
                    if (Math.abs(chunkX - candleChunkX) <= 3 && Math.abs(chunkZ - candleChunkZ) <= 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
