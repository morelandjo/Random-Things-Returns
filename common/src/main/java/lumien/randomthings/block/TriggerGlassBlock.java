package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Glass that, when powered, turns invisible and passable for 3 seconds (chaining to neighbours). */
public class TriggerGlassBlock extends HalfTransparentBlock {
    public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

    public TriggerGlassBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.3F).sound(SoundType.GLASS).noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(TRIGGERED, Boolean.FALSE));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(TRIGGERED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TRIGGERED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide && !state.getValue(TRIGGERED)) {
            BlockState neighborState = level.getBlockState(neighborPos);
            boolean adjacentTriggered = neighborState.getBlock() == this && neighborState.getValue(TRIGGERED);
            if (level.hasNeighborSignal(pos) || adjacentTriggered) {
                level.setBlock(pos, state.setValue(TRIGGERED, true), Block.UPDATE_ALL);
                level.scheduleTick(pos, this, 60);
                triggerAdjacentBlocks(level, pos);
            }
        }
    }

    private void triggerAdjacentBlocks(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.getBlock() == this && !adjacentState.getValue(TRIGGERED)) {
                level.setBlock(adjacentPos, adjacentState.setValue(TRIGGERED, true), Block.UPDATE_ALL);
                level.scheduleTick(adjacentPos, this, 60);
                triggerAdjacentBlocks(level, adjacentPos);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TRIGGERED)) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TRIGGERED) ? Shapes.empty() : super.getCollisionShape(state, level, pos, context);
    }
}
