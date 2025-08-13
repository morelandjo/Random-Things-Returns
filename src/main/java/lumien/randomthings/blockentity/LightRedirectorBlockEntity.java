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

import java.util.*;

public class LightRedirectorBlockEntity extends BlockEntity {
    
    // Static registry of all light redirectors for potential future use
    public static final Set<LightRedirectorBlockEntity> REDIRECTOR_SET = 
            Collections.newSetFromMap(new WeakHashMap<>());
    
    // Map to track which directions are enabled for redirection
    private final Map<Direction, Boolean> enabledMap = new EnumMap<>(Direction.class);
    
    // Map for position targets (for future rendering system integration)
    private final Map<BlockPos, BlockPos> targets = new HashMap<>();
    
    private boolean established = false;

    public LightRedirectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.LIGHT_REDIRECTOR.get(), pos, state);
        
        // Initialize all directions as enabled by default
        for (Direction direction : Direction.values()) {
            enabledMap.put(direction, true);
        }
        
        // Register this redirector in the global set
        synchronized (REDIRECTOR_SET) {
            REDIRECTOR_SET.add(this);
        }
    }

    public void tick() {
        // Basic tick method - can be used for future functionality
    }

    public boolean isEnabled(Direction direction) {
        return enabledMap.getOrDefault(direction, true);
    }

    public void toggleSide(Direction direction) {
        boolean currentState = enabledMap.getOrDefault(direction, true);
        enabledMap.put(direction, !currentState);
        
        // Sync to client
        setChanged();
        if (level != null) {
            if (!level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            } else {
                // Client handler removed - no longer using texture swapping approach
            }
        }
    }

    public void onNeighborChanged() {
        if (level != null && !level.isClientSide()) {
            // Clear targets when neighbors change
            targets.clear();
            
            // Trigger block updates on adjacent positions
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);
                level.sendBlockUpdated(adjacentPos, adjacentState, adjacentState, 3);
            }
        }
    }

    public void onRemoved() {
        // Remove from global registry
        synchronized (REDIRECTOR_SET) {
            REDIRECTOR_SET.remove(this);
        }
        
        if (level != null && !level.isClientSide()) {
            // Notify adjacent blocks that the redirector is gone
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);
                level.sendBlockUpdated(adjacentPos, adjacentState, adjacentState, 3);
            }
        }
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (level != null && level.isClientSide()) {
            established = true;
            // Mark adjacent chunks as needing updates
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                level.getChunkAt(adjacentPos).setUnsaved(true);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save enabled state for each direction
        for (Direction direction : Direction.values()) {
            tag.putBoolean("enabled_" + direction.getName(), 
                          enabledMap.getOrDefault(direction, true));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load enabled state for each direction
        for (Direction direction : Direction.values()) {
            if (tag.contains("enabled_" + direction.getName())) {
                enabledMap.put(direction, tag.getBoolean("enabled_" + direction.getName()));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        // Include enabled states in sync data
        for (Direction direction : Direction.values()) {
            tag.putBoolean("enabled_" + direction.getName(), 
                          enabledMap.getOrDefault(direction, true));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        
        // Clear targets when receiving update
        targets.clear();
        
        // Trigger visual updates on adjacent blocks
        if (level != null && level.isClientSide()) {
            // Client handler removed - no longer using texture swapping approach
            
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = worldPosition.relative(direction);
                BlockState adjacentState = level.getBlockState(adjacentPos);
                level.sendBlockUpdated(adjacentPos, adjacentState, adjacentState, 3);
            }
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Getter methods for potential future use by rendering system
    public Map<Direction, Boolean> getEnabledMap() {
        return Collections.unmodifiableMap(enabledMap);
    }

    public Map<BlockPos, BlockPos> getTargets() {
        return targets;
    }

    public boolean isEstablished() {
        return established;
    }
}