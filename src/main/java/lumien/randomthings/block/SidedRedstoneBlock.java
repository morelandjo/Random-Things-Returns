package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SidedRedstoneBlock extends Block {
    public static final MapCodec<SidedRedstoneBlock> CODEC = simpleCodec(SidedRedstoneBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    @Override
    public MapCodec<SidedRedstoneBlock> codec() {
        return CODEC;
    }

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
        Direction direction = context.getNearestLookingDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        // Output power on the opposite side of the facing direction
        return direction == blockState.getValue(FACING).getOpposite() ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(blockState, level, pos, direction);
    }
}