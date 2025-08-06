package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.BlockBreakerBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public class BlockBreakerBlock extends BaseEntityBlock {
    public static final MapCodec<BlockBreakerBlock> CODEC = simpleCodec(BlockBreakerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    @Override
    public MapCodec<BlockBreakerBlock> codec() {
        return CODEC;
    }

    public BlockBreakerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockBreakerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntityTypes.BLOCK_BREAKER.get(), BlockBreakerBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getNearestLookingDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            setDefaultFacing(level, pos, state);
        }
    }

    private void setDefaultFacing(Level level, BlockPos pos, BlockState state) {
        BlockState northState = level.getBlockState(pos.north());
        BlockState southState = level.getBlockState(pos.south());
        BlockState westState = level.getBlockState(pos.west());
        BlockState eastState = level.getBlockState(pos.east());
        Direction facing = state.getValue(FACING);

        if (facing == Direction.NORTH && northState.canOcclude() && !southState.canOcclude()) {
            facing = Direction.SOUTH;
        } else if (facing == Direction.SOUTH && southState.canOcclude() && !northState.canOcclude()) {
            facing = Direction.NORTH;
        } else if (facing == Direction.WEST && westState.canOcclude() && !eastState.canOcclude()) {
            facing = Direction.EAST;
        } else if (facing == Direction.EAST && eastState.canOcclude() && !westState.canOcclude()) {
            facing = Direction.WEST;
        }

        level.setBlock(pos, state.setValue(FACING, facing), 2);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockBreakerBlockEntity blockBreaker) {
                blockBreaker.neighborChanged();
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}