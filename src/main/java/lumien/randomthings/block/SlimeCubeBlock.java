package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.blockentity.SlimeCubeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SlimeCubeBlock extends BaseEntityBlock {
    public static final MapCodec<SlimeCubeBlock> CODEC = simpleCodec(properties -> new SlimeCubeBlock());
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    
    // Custom bounding box: smaller than a full block (6x6x6 to 10x10x10)
    protected static final VoxelShape SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    
    public SlimeCubeBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(0.6F)
            .sound(SoundType.SLIME_BLOCK)
            .lightLevel(state -> 7) // Provides moderate light
            .noOcclusion()); // Allow light to pass through for translucent appearance
            
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SlimeCubeBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            updatePoweredState(state, level, pos);
        }
    }
    
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);
        if (!level.isClientSide) {
            updatePoweredState(state, level, pos);
        }
    }
    
    private void updatePoweredState(BlockState state, Level level, BlockPos pos) {
        boolean powered = level.hasNeighborSignal(pos);
        boolean currentlyPowered = state.getValue(POWERED);
        
        if (powered != currentlyPowered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 2);
            
            // Notify the block entity about the power state change
            if (level.getBlockEntity(pos) instanceof SlimeCubeBlockEntity slimeCube) {
                slimeCube.setPowered(powered);
            }
        }
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        
        if (level.isClientSide) {
            boolean powered = state.getValue(POWERED);
            
            // Create particle effects around the cube and nearby blocks
            double centerX = pos.getX() + 0.5D;
            double centerY = pos.getY() + 0.5D;
            double centerZ = pos.getZ() + 0.5D;
            
            // Particles around the slime cube itself
            if (random.nextInt(4) == 0) {
                level.addParticle(
                    powered ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.SPORE_BLOSSOM_AIR,
                    centerX + (random.nextDouble() - 0.5D) * 0.8D,
                    centerY + (random.nextDouble() - 0.5D) * 0.8D,
                    centerZ + (random.nextDouble() - 0.5D) * 0.8D,
                    0.0D, 0.02D, 0.0D
                );
            }
            
            // Particles on nearby solid blocks (within 10 block radius)
            if (random.nextInt(8) == 0) {
                int range = 10;
                BlockPos targetPos = pos.offset(
                    random.nextInt(range * 2 + 1) - range,
                    random.nextInt(range * 2 + 1) - range,
                    random.nextInt(range * 2 + 1) - range
                );
                
                if (level.getBlockState(targetPos).isSolidRender(level, targetPos)) {
                    level.addParticle(
                        powered ? ParticleTypes.CRIMSON_SPORE : ParticleTypes.SPORE_BLOSSOM_AIR,
                        targetPos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D,
                        targetPos.getY() + 1.0D + random.nextDouble() * 0.1D,
                        targetPos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D,
                        0.0D, 0.02D, 0.0D
                    );
                }
            }
        }
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.SLIME_CUBE.get(), SlimeCubeBlockEntity::tick);
    }
}