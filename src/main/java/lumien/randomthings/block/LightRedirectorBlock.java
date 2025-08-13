package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.LightRedirectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LightRedirectorBlock extends BaseEntityBlock {
    public static final MapCodec<LightRedirectorBlock> CODEC = simpleCodec(LightRedirectorBlock::new);
    
    // Note: Face states are now handled by BlockEntity only, not BlockState properties
    
    @Override
    public MapCodec<LightRedirectorBlock> codec() {
        return CODEC;
    }

    public LightRedirectorBlock(Properties properties) {
        super(properties);
        // No BlockState properties needed - face states handled by BlockEntity
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // No properties needed - face states handled by BlockEntity
    }
    
    // Removed property helper methods - no longer using BlockState properties

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LightRedirectorBlockEntity lightRedirector) {
                Direction hitFace = hit.getDirection();
                boolean wasEnabled = lightRedirector.isEnabled(hitFace);
                lightRedirector.toggleSide(hitFace);
                
                System.out.println("Toggled " + hitFace + " from " + wasEnabled + " to " + !wasEnabled);
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    // Removed helper method - no longer using BlockState properties
    
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        // Face states handled by BlockEntity - no BlockState sync needed
    }
    
    // Removed getActualState method - no longer using BlockState properties

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, 
                              BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LightRedirectorBlockEntity lightRedirector) {
                lightRedirector.onNeighborChanged();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LightRedirectorBlockEntity lightRedirector) {
                lightRedirector.onRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightRedirectorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use ENTITYBLOCK_ANIMATED so renderer can handle per-face textures
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}