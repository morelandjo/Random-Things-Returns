package lumien.randomthings.block;

import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.menu.AnalogEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import com.mojang.serialization.MapCodec;

public class AnalogEmitterBlock extends BaseEntityBlock {
    public static final MapCodec<AnalogEmitterBlock> CODEC = simpleCodec(properties -> new AnalogEmitterBlock());
    
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public AnalogEmitterBlock() {
        super(Properties.of()
            .strength(3.0F, 5.0F));
        
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnalogEmitterBlockEntity(pos, state);
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
        return this.defaultBlockState().setValue(FACING, 
            Direction.orderedByNearest(context.getPlayer())[0].getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            level.setBlock(pos, state.setValue(FACING, 
                Direction.orderedByNearest(placer)[0].getOpposite()), 2);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Change facing when shift-clicking
                Direction currentFacing = state.getValue(FACING);
                Direction newFacing = hit.getDirection();
                
                if (currentFacing != newFacing) {
                    level.setBlock(pos, state.setValue(FACING, newFacing), 2);
                    
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
                        analogEmitter.neighborChanged();
                    }
                }
            } else {
                // Open GUI
                if (player instanceof ServerPlayer serverPlayer) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
                        serverPlayer.openMenu(analogEmitter, pos);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
            Direction facing = state.getValue(FACING);
            
            // Use ORIGINAL 1.12.2 logic exactly: 
            // If this is NOT the opposite of facing, then output power
            // This means facing.getOpposite() is the input side that doesn't output
            if (facing.getOpposite() != direction) {
                return analogEmitter.getOutput();
            } else {
                return 0;
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
            analogEmitter.neighborChanged();
        }
    }
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            // Force neighbor update when block is placed
            level.updateNeighborsAt(pos, this);
            level.updateNeighbourForOutputSignal(pos, this);
        }
    }
}