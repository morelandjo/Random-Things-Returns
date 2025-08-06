package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public class ContactLeverBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ContactLeverBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(1.5F)
            .sound(SoundType.STONE));
        
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = Direction.getNearest(
            (float)(context.getClickedPos().getX() - context.getPlayer().getX()),
            (float)(context.getClickedPos().getY() - context.getPlayer().getY()),
            (float)(context.getClickedPos().getZ() - context.getPlayer().getZ())
        );
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            Direction facing = Direction.getNearest(
                (float)(pos.getX() - placer.getX()),
                (float)(pos.getY() - placer.getY()),
                (float)(pos.getZ() - placer.getZ())
            );
            level.setBlock(pos, state.setValue(FACING, facing), 2);
            this.setDefaultFacing(level, pos, state.setValue(FACING, facing));
        }
    }

    private void setDefaultFacing(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            BlockState northState = level.getBlockState(pos.north());
            BlockState southState = level.getBlockState(pos.south());
            BlockState westState = level.getBlockState(pos.west());
            BlockState eastState = level.getBlockState(pos.east());
            Direction facing = state.getValue(FACING);

            if (facing == Direction.NORTH && northState.isSolidRender(level, pos.north()) && !southState.isSolidRender(level, pos.south())) {
                facing = Direction.SOUTH;
            } else if (facing == Direction.SOUTH && southState.isSolidRender(level, pos.south()) && !northState.isSolidRender(level, pos.north())) {
                facing = Direction.NORTH;
            } else if (facing == Direction.WEST && westState.isSolidRender(level, pos.west()) && !eastState.isSolidRender(level, pos.east())) {
                facing = Direction.EAST;
            } else if (facing == Direction.EAST && eastState.isSolidRender(level, pos.east()) && !westState.isSolidRender(level, pos.west())) {
                facing = Direction.WEST;
            }

            level.setBlock(pos, state.setValue(FACING, facing), 2);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            this.activate(level, pos, null);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getValue(POWERED) && !newState.is(this)) {
            this.updateNeighbors(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public void activate(Level level, BlockPos pos, Direction fromFacing) {
        BlockState state = level.getBlockState(pos);
        boolean wasPowered = state.getValue(POWERED);
        BlockState newState = state.setValue(POWERED, !wasPowered);
        
        level.setBlock(pos, newState, 3);
        this.updateNeighbors(level, pos, fromFacing != null ? fromFacing : state.getValue(FACING));
        
        float pitch = !wasPowered ? 0.6F : 0.5F;
        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, pitch);
    }

    private void updateNeighbors(Level level, BlockPos pos, Direction facing) {
        level.updateNeighborsAt(pos, this);
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
    }
}