package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class PeaceCandleBlockEntity extends BlockEntity {
    // Global collection of all peace candles for spawn prevention system
    public static final Set<PeaceCandleBlockEntity> CANDLES = Collections.newSetFromMap(new WeakHashMap<>());
    
    public PeaceCandleBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.PEACE_CANDLE.get(), pos, blockState);
        
        // Add this candle to the global collection
        synchronized (CANDLES) {
            CANDLES.add(this);
        }
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Peace candle has no data to save - it just exists
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Peace candle has no data to load - it just exists
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, PeaceCandleBlockEntity blockEntity) {
        // Peace candles don't need active ticking - they work passively via the spawn prevention system
        // The actual spawn prevention is handled by the event system that checks the CANDLES collection
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        
        // Remove from global collection when block is removed
        synchronized (CANDLES) {
            CANDLES.remove(this);
        }
    }
    
    @Override
    public void onChunkUnloaded() {
        // Note: Don't remove from CANDLES here - chunk unloading is temporary
        // The WeakHashMap will handle cleanup if the BlockEntity gets GC'd
    }
    
    /**
     * Checks if the given chunk position is within the peace radius of any active peace candle.
     * Peace candles prevent mob spawning in a 3-chunk radius (7x7 chunk area total).
     * 
     * @param chunkX The chunk X coordinate to check
     * @param chunkZ The chunk Z coordinate to check
     * @return true if mob spawning should be prevented in this chunk
     */
    public static boolean isPeacefulChunk(int chunkX, int chunkZ) {
        synchronized (CANDLES) {
            for (PeaceCandleBlockEntity candle : CANDLES) {
                if (!candle.isRemoved() && candle.getLevel() != null) {
                    // Calculate chunk coordinates of the candle
                    int candleChunkX = candle.getBlockPos().getX() >> 4;
                    int candleChunkZ = candle.getBlockPos().getZ() >> 4;
                    
                    // Check if the chunk is within 3-chunk radius
                    int deltaX = Math.abs(chunkX - candleChunkX);
                    int deltaZ = Math.abs(chunkZ - candleChunkZ);
                    
                    if (deltaX <= 3 && deltaZ <= 3) {
                        return true; // This chunk is peaceful
                    }
                }
            }
        }
        return false; // No peace candles nearby
    }
}