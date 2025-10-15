package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.SpectreCoilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

/**
 * Spectre Coil - Transfers energy from player's Spectre Energy buffer to adjacent machines.
 * Available in 5 tiers with different transfer rates and capabilities.
 */
public class SpectreCoilBlock extends BaseEntityBlock {
    public static final MapCodec<SpectreCoilBlock> CODEC = simpleCodec(p -> new SpectreCoilBlock(CoilType.NORMAL));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // Bounding boxes for each direction
    protected static final VoxelShape NORTH_AABB = Block.box(5, 5, 15, 11, 11, 16);
    protected static final VoxelShape SOUTH_AABB = Block.box(5, 5, 0, 11, 11, 1.5);
    protected static final VoxelShape WEST_AABB = Block.box(15, 5, 5, 16, 11, 11);
    protected static final VoxelShape EAST_AABB = Block.box(0, 5, 5, 1.5, 11, 11);
    protected static final VoxelShape UP_AABB = Block.box(5, 0, 5, 11, 1.5, 11);
    protected static final VoxelShape DOWN_AABB = Block.box(5, 15, 5, 11, 16, 11);

    private final CoilType coilType;

    public enum CoilType {
        NORMAL("normal", new Color(0, 255, 255).getRGB(), 1024),        // Cyan - 1024 RF/t
        REDSTONE("redstone", Color.RED.getRGB(), 4096),                  // Red - 4096 RF/t
        ENDER("ender", new Color(200, 0, 210).getRGB(), 20480),         // Purple - 20480 RF/t
        NUMBER("number", Color.GREEN.getRGB(), 1000000),                 // Green - 1M RF/t (configurable)
        GENESIS("genesis", Color.ORANGE.getRGB(), 10000000);            // Orange - Infinite (creative)

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
            .lightLevel(state -> 7)); // Glowing effect
        this.coilType = type;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public CoilType getCoilType() {
        return coilType;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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

        // Check if we can attach to the clicked face
        if (canAttachTo(context.getLevel(), context.getClickedPos(), direction.getOpposite())) {
            return this.defaultBlockState().setValue(FACING, direction);
        }

        // Try to find another valid face
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

        if (!level.isClientSide && placer instanceof Player player) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpectreCoilBlockEntity coil) {
                coil.setOwner(player.getUUID());
                coil.setCoilType(this.coilType);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        // Check if still attached to a valid energy storage
        Direction facing = state.getValue(FACING);
        if (direction == facing.getOpposite() && !canAttachTo(level, pos, facing.getOpposite())) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    /**
     * Check if the coil can attach to a block that has energy storage capability.
     */
    private boolean canAttachTo(LevelAccessor level, BlockPos coilPos, Direction attachDirection) {
        BlockPos attachPos = coilPos.relative(attachDirection);
        BlockEntity be = level.getBlockEntity(attachPos);

        if (be == null) {
            return false;
        }

        // Check if the block has energy storage capability
        if (level instanceof Level realLevel) {
            IEnergyStorage storage = realLevel.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                attachPos,
                null,
                null,
                attachDirection.getOpposite()
            );
            return storage != null;
        }

        return false;
    }
}
