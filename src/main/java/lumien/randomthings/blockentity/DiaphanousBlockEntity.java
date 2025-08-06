package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class DiaphanousBlockEntity extends BlockEntity {
    private BlockState displayState = Blocks.STONE.defaultBlockState();
    private Map<Direction, Boolean> renderMap = new HashMap<>();
    private boolean isItem = false;
    private boolean inverted = false;

    public DiaphanousBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.DIAPHANOUS_BLOCK.get(), pos, state);
        
        // Initialize render map
        for (Direction direction : Direction.values()) {
            renderMap.put(direction, true);
        }
        
    }

    public BlockState getDisplayState() {
        return displayState;
    }

    public void setDisplayState(BlockState displayState) {
        this.displayState = displayState;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        setChanged();
    }

    public boolean isItem() {
        return isItem;
    }

    public void setItem(boolean isItem) {
        this.isItem = isItem;
    }

    public boolean shouldRenderSide(Direction direction) {
        return renderMap.getOrDefault(direction, true);
    }

    public Map<Direction, Boolean> getRenderMap() {
        return renderMap;
    }

    public void updateRenderMap() {
        if (level == null) return;
        
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = worldPosition.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            
            // Hide faces adjacent to solid blocks or other diaphanous blocks
            boolean shouldRender = !neighborState.isSolidRender(level, neighborPos) && 
                                 neighborState.getBlock() != ModBlocks.DIAPHANOUS_BLOCK.get();
            renderMap.put(direction, shouldRender);
        }
        
        setChanged();
        if (!level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // Save render map
        for (Direction direction : Direction.values()) {
            tag.putBoolean(direction.getName().toLowerCase(), renderMap.getOrDefault(direction, true));
        }
        
        // Save display state
        if (displayState != null) {
            ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(displayState.getBlock());
            if (blockId != null) {
                tag.putString("block", blockId.toString());
            }
        }
        
        tag.putBoolean("inverted", inverted);
        tag.putBoolean("isItem", isItem);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // Load render map
        for (Direction direction : Direction.values()) {
            renderMap.put(direction, tag.getBoolean(direction.getName().toLowerCase()));
        }
        
        // Load display state
        if (tag.contains("block")) {
            try {
                ResourceLocation blockId = ResourceLocation.parse(tag.getString("block"));
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockId);
                if (block != null) {
                    displayState = block.defaultBlockState();
                } else {
                    displayState = Blocks.STONE.defaultBlockState();
                }
            } catch (Exception e) {
                displayState = Blocks.STONE.defaultBlockState();
            }
        }
        
        inverted = tag.getBoolean("inverted");
        isItem = tag.getBoolean("isItem");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        // Include all data for client sync
        for (Direction direction : Direction.values()) {
            tag.putBoolean(direction.getName().toLowerCase(), renderMap.getOrDefault(direction, true));
        }
        
        if (displayState != null) {
            ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(displayState.getBlock());
            if (blockId != null) {
                tag.putString("block", blockId.toString());
            }
        }
        
        tag.putBoolean("inverted", inverted);
        tag.putBoolean("isItem", isItem);
        
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DiaphanousBlockEntity blockEntity) {
        // Tick logic if needed for animations or updates
    }
}