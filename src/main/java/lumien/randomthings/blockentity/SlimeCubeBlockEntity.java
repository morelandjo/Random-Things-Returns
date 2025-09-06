package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class SlimeCubeBlockEntity extends BlockEntity {
    
    // Static tracking of all slime cubes for efficient spawn checking
    // Using WeakHashMap to prevent memory leaks when chunks unload
    public static final Set<SlimeCubeBlockEntity> cubes = Collections.newSetFromMap(new WeakHashMap<>());
    
    private boolean powered = false;
    
    public SlimeCubeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.SLIME_CUBE.get(), pos, blockState);
        
        // Register this slime cube in the global set
        synchronized (cubes) {
            cubes.add(this);
        }
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, SlimeCubeBlockEntity blockEntity) {
        // Static tick method for block entity ticking
        // No continuous operations needed - the slime cube works through events
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.powered = tag.getBoolean("powered");
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("powered", this.powered);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("powered", this.powered);
        return tag;
    }
    
    public void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            setChanged();
        }
    }
    
    public boolean isPowered() {
        return this.powered;
    }
    
    public ChunkPos getChunkPos() {
        return new ChunkPos(this.worldPosition);
    }
    
    /**
     * Check if there's an active (unpowered) slime cube in the given chunk
     * @param chunkPos The chunk position to check
     * @return true if there's an active slime cube in this chunk
     */
    public static boolean hasActiveSlimeCubeInChunk(ChunkPos chunkPos) {
        synchronized (cubes) {
            for (SlimeCubeBlockEntity cube : cubes) {
                if (cube.isRemoved()) {
                    continue;
                }
                
                ChunkPos cubeChunk = cube.getChunkPos();
                if (cubeChunk.equals(chunkPos) && !cube.isPowered()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Check if there's a powered slime cube in the given chunk that should prevent spawning
     * @param chunkPos The chunk position to check
     * @return true if there's a powered slime cube preventing spawns in this chunk
     */
    public static boolean hasPoweredSlimeCubeInChunk(ChunkPos chunkPos) {
        synchronized (cubes) {
            for (SlimeCubeBlockEntity cube : cubes) {
                if (cube.isRemoved()) {
                    continue;
                }
                
                ChunkPos cubeChunk = cube.getChunkPos();
                if (cubeChunk.equals(chunkPos) && cube.isPowered()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        // Remove from global tracking when block entity is removed
        synchronized (cubes) {
            cubes.remove(this);
        }
    }
}