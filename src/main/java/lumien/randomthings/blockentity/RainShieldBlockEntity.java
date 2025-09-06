package lumien.randomthings.blockentity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RainShieldBlockEntity extends BlockEntity {
    
    // Configuration - 5 chunks = 80 blocks (16 blocks per chunk)
    private static final int RAIN_SHIELD_RANGE = 80;
    
    // Static tracking of all rain shields for efficient rain checking
    public static final Set<RainShieldBlockEntity> shields = Collections.newSetFromMap(new WeakHashMap<>());
    
    // Cache for rain checking to improve performance
    public static final ConcurrentHashMap<BlockPos, Boolean> rainCache = new ConcurrentHashMap<>();
    
    private boolean active = true;
    
    public RainShieldBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.RAIN_SHIELD.get(), pos, blockState);
        
        synchronized (shields) {
            shields.add(this);
        }
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.active = tag.getBoolean("active");
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("active", this.active);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        boolean oldActive = this.active;
        this.active = tag.getBoolean("active");
        
    }
    
    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        handleUpdateTag(pkt.getTag(), registries);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("active", this.active);
    }
    
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.setRemoved();
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        synchronized (shields) {
            shields.remove(this);
        }
    }
    
    /**
     * Main method to check if it should rain at a specific position.
     * This is called by the rain prevention system.
     */
    public static boolean shouldRain(Level level, BlockPos pos) {
        // Check Rain Shield states first, then use cache only if no shields are nearby
        synchronized (shields) {
            for (RainShieldBlockEntity rainShield : shields) {
                if (rainShield.level == level && 
                    !rainShield.isRemoved() && 
                    isInRange(rainShield.getBlockPos(), pos, RAIN_SHIELD_RANGE)) {
                    
                    
                    if (rainShield.active) {
                        // Rain shield is active and blocking rain at this position
                        return false;
                    } else {
                        // Rain shield is inactive, allow rain
                        return true;
                    }
                }
            }
        }
        
        // No rain shields in range, allow rain
        return true;
    }
    
    /**
     * Check if a position is within range of a rain shield.
     * Uses 2D distance calculation (ignoring Y coordinate).
     */
    private static boolean isInRange(BlockPos shieldPos, BlockPos targetPos, int range) {
        // Convert to 2D coordinates (ignore Y)
        BlockPos shield2D = new BlockPos(shieldPos.getX(), 0, shieldPos.getZ());
        BlockPos target2D = new BlockPos(targetPos.getX(), 0, targetPos.getZ());
        
        return shield2D.distSqr(target2D) < (range * range);
    }
    
    /**
     * Called when the block is placed
     */
    public void onBlockAdded(Level level, BlockPos pos, BlockState state) {
        // Check if the rain shield should be active based on redstone power
        boolean hasSignal = level.hasNeighborSignal(pos);
        this.active = !hasSignal;
        setChanged();
        
    }
    
    /**
     * Called when neighboring blocks change (for redstone detection)
     */
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        boolean hasSignal = level.hasNeighborSignal(pos);
        boolean desiredState = !hasSignal;
        
        if (desiredState != this.active) {
            this.active = desiredState;
            setChanged();
            
            
            // Force a block update and sync to clients
            if (level != null) {
                if (!level.isClientSide) {
                    // Server side - send update to clients
                    level.sendBlockUpdated(pos, state, state, 3);
                } else {
                    // Client side - trigger block update
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }
    
    /**
     * Clear the rain cache. This should be called periodically to prevent memory leaks
     * and ensure that changes in rain shield configuration are properly reflected.
     */
    public static void clearRainCache() {
        rainCache.clear();
    }
    
    public boolean isActive() {
        return active;
    }
}