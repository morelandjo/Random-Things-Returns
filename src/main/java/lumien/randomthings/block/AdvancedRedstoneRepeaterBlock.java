package lumien.randomthings.block;

import lumien.randomthings.blockentity.AdvancedRedstoneRepeaterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ticks.TickPriority;
import org.joml.Vector3f;

public class AdvancedRedstoneRepeaterBlock extends BaseEntityBlock {
    public static final MapCodec<AdvancedRedstoneRepeaterBlock> CODEC = simpleCodec(properties -> new AdvancedRedstoneRepeaterBlock(false));
    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    protected final boolean isRepeaterPowered;

    public AdvancedRedstoneRepeaterBlock(boolean powered) {
        super(Properties.of()
            .strength(0.0F)
            .noOcclusion()
            .noLootTable());
        
        this.isRepeaterPowered = powered;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(POWERED, powered)
            .setValue(LOCKED, false));
    }

    @Override
    public MapCodec<AdvancedRedstoneRepeaterBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, LOCKED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, 
                                          BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AdvancedRedstoneRepeaterBlockEntity && player instanceof ServerPlayer) {
                ((ServerPlayer) player).openMenu((AdvancedRedstoneRepeaterBlockEntity) blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedRedstoneRepeaterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (this.shouldBePowered(level, pos, state)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        this.updateNeighborsInFront(level, pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getSignal(level, pos, direction);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!this.isPowered(state)) {
            return 0;
        }
        return state.getValue(FACING) == direction ? this.getActiveSignal(level, pos, state) : 0;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (this.canSurvive(state, level, pos)) {
            this.updateState(level, pos, state);
        } else {
            // Drop the item
            popResource(level, pos, new ItemStack(ModBlocks.ADVANCED_REDSTONE_REPEATER.get()));
            level.removeBlock(pos, false);
            this.updateNeighborsInFront(level, pos, state);
        }
    }

    protected void updateState(Level level, BlockPos pos, BlockState state) {
        if (!this.isLocked(level, pos, state)) {
            boolean shouldBePowered = this.shouldBePowered(level, pos, state);

            if (this.isRepeaterPowered != shouldBePowered && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                TickPriority priority = TickPriority.NORMAL;
                if (this.isFacingTowardsRepeater(level, pos, state)) {
                    priority = TickPriority.EXTREMELY_HIGH;
                } else if (this.isRepeaterPowered) {
                    priority = TickPriority.HIGH;
                }

                level.scheduleTick(pos, this, getDelay(level, pos, this.isRepeaterPowered, shouldBePowered), priority);
            }
        }
    }

    protected boolean shouldBePowered(Level level, BlockPos pos, BlockState state) {
        return this.calculateInputStrength(level, pos, state) > 0;
    }

    protected int calculateInputStrength(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos inputPos = pos.relative(facing);
        int power = level.getSignal(inputPos, facing);

        if (power >= 15) {
            return power;
        } else {
            BlockState inputState = level.getBlockState(inputPos);
            return Math.max(power, inputState.getBlock() == Blocks.REDSTONE_WIRE ? inputState.getValue(RedStoneWireBlock.POWER) : 0);
        }
    }

    protected boolean isLocked(Level level, BlockPos pos, BlockState state) {
        return this.getPowerOnSides(level, pos, state) > 0;
    }

    protected int getPowerOnSides(BlockGetter level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction left = facing.getClockWise();
        Direction right = facing.getCounterClockWise();
        return Math.max(this.getPowerOnSide(level, pos.relative(left), left), this.getPowerOnSide(level, pos.relative(right), right));
    }

    protected int getPowerOnSide(BlockGetter level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (this.isAlternateInput(state)) {
            if (block == Blocks.REDSTONE_BLOCK) {
                return 15;
            } else {
                return block == Blocks.REDSTONE_WIRE ? state.getValue(RedStoneWireBlock.POWER) : 0;
            }
        } else {
            return 0;
        }
    }

    protected boolean isAlternateInput(BlockState state) {
        return isDiode(state);
    }

    public static boolean isDiode(BlockState state) {
        return state.is(ModBlocks.ADVANCED_REDSTONE_REPEATER.get()) || 
               state.is(ModBlocks.ADVANCED_REDSTONE_REPEATER_POWERED.get());
    }

    protected boolean isFacingTowardsRepeater(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING).getOpposite();
        BlockPos checkPos = pos.relative(facing);

        if (isDiode(level.getBlockState(checkPos))) {
            return level.getBlockState(checkPos).getValue(FACING) != facing;
        }
        return false;
    }

    protected int getActiveSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return 15;
    }

    protected boolean isPowered(BlockState state) {
        return this.isRepeaterPowered;
    }

    protected int getDelay(BlockGetter level, BlockPos pos, boolean oldState, boolean newState) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AdvancedRedstoneRepeaterBlockEntity arr) {
            if (oldState && !newState) {
                return arr.getTurnOffDelay();
            } else {
                return arr.getTurnOnDelay();
            }
        }
        return 20;
    }

    protected BlockState getPoweredState(BlockState unpoweredState) {
        Direction facing = unpoweredState.getValue(FACING);
        Boolean locked = unpoweredState.getValue(LOCKED);
        return ModBlocks.ADVANCED_REDSTONE_REPEATER_POWERED.get().defaultBlockState()
            .setValue(FACING, facing)
            .setValue(LOCKED, locked)
            .setValue(POWERED, true);
    }

    protected BlockState getUnpoweredState(BlockState poweredState) {
        Direction facing = poweredState.getValue(FACING);
        Boolean locked = poweredState.getValue(LOCKED);
        return ModBlocks.ADVANCED_REDSTONE_REPEATER.get().defaultBlockState()
            .setValue(FACING, facing)
            .setValue(LOCKED, locked)
            .setValue(POWERED, false);
    }

    protected void preserveBlockEntityData(Level level, BlockPos pos, BlockState oldState, BlockState newState) {
        // Store block entity data
        BlockEntity oldBe = level.getBlockEntity(pos);
        if (oldBe instanceof AdvancedRedstoneRepeaterBlockEntity oldArr) {
            int turnOnDelay = oldArr.getTurnOnDelay();
            int turnOffDelay = oldArr.getTurnOffDelay();
            
            // Change the block state
            level.setBlock(pos, newState, 2);
            
            // Restore data to new block entity
            BlockEntity newBe = level.getBlockEntity(pos);
            if (newBe instanceof AdvancedRedstoneRepeaterBlockEntity newArr) {
                newArr.setTurnOnDelay(turnOnDelay);
                newArr.setTurnOffDelay(turnOffDelay);
            }
        } else {
            // Fallback if no block entity exists
            level.setBlock(pos, newState, 2);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.isLocked(level, pos, state)) {
            boolean shouldBePowered = this.shouldBePowered(level, pos, state);

            if (this.isRepeaterPowered && !shouldBePowered) {
                this.preserveBlockEntityData(level, pos, state, this.getUnpoweredState(state));
            } else if (!this.isRepeaterPowered) {
                this.preserveBlockEntityData(level, pos, state, this.getPoweredState(state));

                if (!shouldBePowered) {
                    level.scheduleTick(pos, this.getPoweredState(state).getBlock(), 
                        getDelay(level, pos, true, false));
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (this.isRepeaterPowered) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5F + (random.nextFloat() - 0.5F) * 0.2D;
            double y = pos.getY() + 0.4F + (random.nextFloat() - 0.5F) * 0.2D;
            double z = pos.getZ() + 0.5F + (random.nextFloat() - 0.5F) * 0.2D;
            float offset = -5.0F / 16.0F;
            double dx = offset * facing.getStepX();
            double dz = offset * facing.getStepZ();
            level.addParticle(DustParticleOptions.REDSTONE, x + dx, y, z + dz, 0.0D, 0.0D, 0.0D);
        }
    }

    private void updateNeighborsInFront(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        BlockPos frontPos = pos.relative(facing.getOpposite());
        level.neighborChanged(frontPos, this, pos);
        level.updateNeighborsAtExceptFromFacing(frontPos, this, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        boolean locked = this.getPowerOnSides(level, pos, state) > 0;
        return state.setValue(LOCKED, locked);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return canSupportRigidBlock(level, pos.below());
    }
}