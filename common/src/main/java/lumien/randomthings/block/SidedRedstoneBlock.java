package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Redstone that only outputs on the face opposite its placement direction. */
public class SidedRedstoneBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public SidedRedstoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == blockState.getValue(FACING).getOpposite() ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(blockState, level, pos, direction);
    }
}
