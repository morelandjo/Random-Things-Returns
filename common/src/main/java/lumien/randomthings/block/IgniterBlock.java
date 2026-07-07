package lumien.randomthings.block;

import dev.architectury.registry.menu.MenuRegistry;
import lumien.randomthings.blockentity.IgniterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Places (or removes) fire in front of itself based on redstone, per its configured mode. */
public class IgniterBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public IgniterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IgniterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, Direction.orderedByNearest(context.getPlayer())[0].getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            level.setBlock(pos, state.setValue(FACING, Direction.orderedByNearest(placer)[0].getOpposite()), 2);
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            setDefaultFacing(level, pos, state);
            if (level.getBlockEntity(pos) instanceof IgniterBlockEntity igniter) {
                igniter.onBlockPlaced();
            }
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
            && level.getBlockEntity(pos) instanceof IgniterBlockEntity igniter) {
            MenuRegistry.openExtendedMenu(serverPlayer, igniter);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof IgniterBlockEntity igniter) {
            igniter.neighborChanged(state, level, pos, neighborBlock, neighborPos);
        }
    }
}
