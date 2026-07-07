package lumien.randomthings.block;

import lumien.randomthings.blockentity.SoundBoxBlockEntity;
import lumien.randomthings.item.ItemSoundPattern;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class SoundBoxBlock extends BaseEntityBlock {
    public static final BooleanProperty HAS_PATTERN = BooleanProperty.create("has_pattern");

    public SoundBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_PATTERN, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_PATTERN);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoundBoxBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Filled box: right-click (any item) ejects the stored pattern.
        if (state.getValue(HAS_PATTERN)) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof SoundBoxBlockEntity soundBox) {
                ItemStack ejected = soundBox.getPattern();
                soundBox.setPattern(ItemStack.EMPTY);
                if (!ejected.isEmpty()) {
                    Block.popResource(level, pos.above(), ejected);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Empty box: insert a configured Sound Pattern.
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.SOUND_PATTERN.get()) || ItemSoundPattern.getSoundLocation(stack) == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SoundBoxBlockEntity soundBox) {
            soundBox.setPattern(stack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof SoundBoxBlockEntity soundBox) {
            soundBox.onNeighborChanged();
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SoundBoxBlockEntity soundBox) {
            soundBox.initRedstoneState();
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof SoundBoxBlockEntity soundBox) {
            ItemStack pattern = soundBox.getPattern();
            if (!pattern.isEmpty()) {
                Block.popResource(level, pos, pattern);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
