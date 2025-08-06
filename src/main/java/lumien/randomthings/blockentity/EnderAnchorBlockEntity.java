package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;

public class EnderAnchorBlockEntity extends BlockEntity {
    
    private boolean firstTick = true;
    private boolean isChunkForced = false;
    
    public EnderAnchorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ENDER_ANCHOR.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isChunkForced", isChunkForced);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        isChunkForced = tag.getBoolean("isChunkForced");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnderAnchorBlockEntity blockEntity) {
        if (!level.isClientSide && blockEntity.firstTick) {
            blockEntity.firstTick = false;
            blockEntity.setupChunkLoading((ServerLevel) level);
        }
    }

    private void setupChunkLoading(ServerLevel level) {
        // Check if chunk loading is enabled via config
        // For now, we'll implement basic chunk loading - this can be expanded later with config
        if (!isChunkForced) {
            try {
                // Force load the chunk containing this anchor
                level.setChunkForced(worldPosition.getX() >> 4, worldPosition.getZ() >> 4, true);
                isChunkForced = true;
                setChanged();
            } catch (Exception e) {
                // Handle any chunk loading errors gracefully
            }
        }
    }

    public void discardTicket() {
        if (level instanceof ServerLevel serverLevel && isChunkForced) {
            try {
                serverLevel.setChunkForced(worldPosition.getX() >> 4, worldPosition.getZ() >> 4, false);
                isChunkForced = false;
            } catch (Exception e) {
                // Handle any errors gracefully
            }
        }
    }
}