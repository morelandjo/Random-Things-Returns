package lumien.randomthings.block;

import lumien.randomthings.blockentity.AdvancedRedstoneTorchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** Wall-mounted variant of the Advanced Redstone Torch. */
public class AdvancedRedstoneWallTorchBlock extends AdvancedRedstoneTorchBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public AdvancedRedstoneWallTorchBlock() {
        super(BlockBehaviour.Properties.of().noCollission().instabreak().lightLevel(state -> 7).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(COLOR_PROPERTY, COLOR.RED));
    }

    @Override
    public String getDescriptionId() {
        return this.asItem().getDescriptionId();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return WallTorchBlock.getShape(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockpos = pos.relative(direction.getOpposite());
        return level.getBlockState(blockpos).isFaceSturdy(level, blockpos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)
            ? Blocks.AIR.defaultBlockState() : state;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(context);
        return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(WallTorchBlock.FACING));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING).getOpposite();
        double d1 = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * direction.getStepX();
        double d2 = pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D + 0.22D;
        double d3 = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D + 0.27D * direction.getStepZ();
        if (state.getValue(COLOR_PROPERTY) == COLOR.RED) {
            level.addParticle(DustParticleOptions.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
        } else {
            level.addParticle(GREEN_DUST, d1, d2, d3, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        AdvancedRedstoneTorchBlock.update(state, level, pos, random, this.shouldBeGreen(level, pos, state));
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (blockAccess.getBlockEntity(pos) instanceof AdvancedRedstoneTorchBlockEntity te) {
            int strength = blockState.getValue(COLOR_PROPERTY) == COLOR.RED ? te.signalStrengthRed() : te.signalStrengthGreen();
            return blockState.getValue(FACING) != side ? strength : 0;
        }
        return 0;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, COLOR_PROPERTY);
    }
}
