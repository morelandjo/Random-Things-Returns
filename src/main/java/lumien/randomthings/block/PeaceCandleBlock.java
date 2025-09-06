package lumien.randomthings.block;

import lumien.randomthings.blockentity.PeaceCandleBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nullable;

public class PeaceCandleBlock extends BaseEntityBlock {
    public static final MapCodec<PeaceCandleBlock> CODEC = simpleCodec(properties -> new PeaceCandleBlock());
    
    // Custom bounding box - small candle shape (0.3125 to 0.6875 on X/Z, 0 to 0.125 on Y + candle height)
    protected static final VoxelShape SHAPE = Shapes.or(
        Block.box(6.0, 0.0, 6.0, 10.0, 1.0, 10.0), // Base
        Block.box(7.5, 1.0, 7.5, 8.5, 4.0, 8.5),   // Candle
        Block.box(5.0, 1.0, 6.0, 6.0, 2.0, 10.0),  // Wax drip 1
        Block.box(10.0, 1.0, 6.0, 11.0, 2.0, 10.0), // Wax drip 2
        Block.box(6.0, 1.0, 5.0, 10.0, 2.0, 6.0),  // Wax drip 3
        Block.box(6.0, 1.0, 10.0, 10.0, 2.0, 11.0) // Wax drip 4
    );
    
    public PeaceCandleBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.0F)
            .sound(SoundType.STONE)
            .lightLevel(state -> 14) // Provides light like a torch
            .noOcclusion()); // Allow light to pass through
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PeaceCandleBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        
        if (level.isClientSide) {
            // Add flame particles above the candle
            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.3D; // Above the candle
            double z = pos.getZ() + 0.5D;
            
            // Create a simple flame particle effect
            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, 
                x + (random.nextDouble() - 0.5D) * 0.1D, 
                y + random.nextDouble() * 0.1D, 
                z + (random.nextDouble() - 0.5D) * 0.1D, 
                0.0D, 0.0D, 0.0D);
                
            // Occasionally add smoke
            if (random.nextInt(3) == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, 
                    x + (random.nextDouble() - 0.5D) * 0.1D, 
                    y + random.nextDouble() * 0.1D, 
                    z + (random.nextDouble() - 0.5D) * 0.1D, 
                    0.0D, 0.01D, 0.0D);
            }
        }
    }
    
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        
        if (!this.canSurvive(state, level, pos)) {
            // Drop the block if it can't survive
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntityTypes.PEACE_CANDLE.get(), PeaceCandleBlockEntity::serverTick);
    }
}