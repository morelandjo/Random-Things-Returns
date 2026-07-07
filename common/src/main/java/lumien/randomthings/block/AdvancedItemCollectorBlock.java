package lumien.randomthings.block;

import dev.architectury.registry.menu.MenuRegistry;
import lumien.randomthings.blockentity.AdvancedItemCollectorBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AdvancedItemCollectorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    private static final VoxelShape SHAPE_UP    = Block.box(6, 0, 6, 10, 5, 10);
    private static final VoxelShape SHAPE_DOWN  = Block.box(6, 11, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_NORTH = Block.box(6, 6, 11, 10, 10, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.box(6, 6, 0, 10, 10, 5);
    private static final VoxelShape SHAPE_WEST  = Block.box(11, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_EAST  = Block.box(0, 6, 6, 5, 10, 10);

    public AdvancedItemCollectorBlock() {
        super(Properties.of()
            .strength(0.3F)
            .sound(net.minecraft.world.level.block.SoundType.STONE)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedItemCollectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntityTypes.ADVANCED_ITEM_COLLECTOR.get(), AdvancedItemCollectorBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clicked = context.getClickedFace();
        if (hasInventoryOn(level, pos, clicked.getOpposite())) {
            return this.defaultBlockState().setValue(FACING, clicked);
        }
        for (Direction d : Direction.values()) {
            if (hasInventoryOn(level, pos, d.getOpposite())) {
                return this.defaultBlockState().setValue(FACING, d);
            }
        }
        return this.defaultBlockState().setValue(FACING, Direction.DOWN);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasInventoryOn(level, pos, state.getValue(FACING).getOpposite());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction backing = state.getValue(FACING).getOpposite();
        if (direction == backing && !hasInventoryOn(level, pos, backing)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    private static boolean hasInventoryOn(LevelReader level, BlockPos pos, Direction toward) {
        if (!(level instanceof Level realLevel)) return true;
        return RTContainers.getContainerAt(realLevel, pos.relative(toward)) != null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedItemCollectorBlockEntity collector) {
                MenuRegistry.openExtendedMenu(serverPlayer, collector);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
