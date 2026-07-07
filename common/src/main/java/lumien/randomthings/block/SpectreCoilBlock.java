package lumien.randomthings.block;

import lumien.randomthings.blockentity.SpectreCoilBlockEntity;
import lumien.randomthings.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Spectre Coil — attaches to a machine and feeds it from the owner's Spectre Energy buffer.
 * Five tiers with different transfer rates; NUMBER/GENESIS are creative sources.
 */
public class SpectreCoilBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected static final VoxelShape NORTH_AABB = Block.box(5, 5, 15, 11, 11, 16);
    protected static final VoxelShape SOUTH_AABB = Block.box(5, 5, 0, 11, 11, 1.5);
    protected static final VoxelShape WEST_AABB = Block.box(15, 5, 5, 16, 11, 11);
    protected static final VoxelShape EAST_AABB = Block.box(0, 5, 5, 1.5, 11, 11);
    protected static final VoxelShape UP_AABB = Block.box(5, 0, 5, 11, 1.5, 11);
    protected static final VoxelShape DOWN_AABB = Block.box(5, 15, 5, 11, 16, 11);

    private final CoilType coilType;

    public enum CoilType {
        NORMAL("normal", 0x00FFFF, 1024),
        REDSTONE("redstone", 0xFF0000, 4096),
        ENDER("ender", 0xC800D2, 20480),
        NUMBER("number", 0x00FF00, 1_000_000),
        GENESIS("genesis", 0xFFC800, 10_000_000);

        private final String name;
        private final int color;
        private final int transferRate;

        CoilType(String name, int color, int transferRate) {
            this.name = name;
            this.color = color;
            this.transferRate = transferRate;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }

        public int getTransferRate() {
            return transferRate;
        }
    }

    public SpectreCoilBlock(CoilType type) {
        super(Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(0.3f)
            .sound(SoundType.STONE)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false)
            .lightLevel(state -> 7));
        this.coilType = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public CoilType getCoilType() {
        return coilType;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case WEST -> WEST_AABB;
            case EAST -> EAST_AABB;
            case UP -> UP_AABB;
            case DOWN -> DOWN_AABB;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpectreCoilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : SpectreCoilBlockEntity.createTicker();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        if (canAttachTo(context.getLevel(), context.getClickedPos(), direction.getOpposite())) {
            return this.defaultBlockState().setValue(FACING, direction);
        }
        for (Direction dir : Direction.values()) {
            if (canAttachTo(context.getLevel(), context.getClickedPos(), dir.getOpposite())) {
                return this.defaultBlockState().setValue(FACING, dir);
            }
        }
        return this.defaultBlockState().setValue(FACING, Direction.UP);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
            && level.getBlockEntity(pos) instanceof SpectreCoilBlockEntity coil) {
            coil.setOwner(player.getUUID());
            coil.setCoilType(this.coilType);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !canAttachTo(level, pos, facing.getOpposite())) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    /**
     * The coil attaches to blocks that can receive energy, or (so placement stays sensible on
     * loaders without an energy API present) to any sturdy face.
     */
    private boolean canAttachTo(LevelAccessor level, BlockPos coilPos, Direction attachDirection) {
        BlockPos attachPos = coilPos.relative(attachDirection);
        if (level instanceof Level realLevel
            && Services.ENERGY.canReceiveEnergy(realLevel, attachPos, attachDirection.getOpposite())) {
            return true;
        }
        return level.getBlockState(attachPos).isFaceSturdy(level, attachPos, attachDirection.getOpposite());
    }
}
