package lumien.randomthings.block;

import lumien.randomthings.blockentity.DiaphanousBlockEntity;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Looks like another block but has no collision (or ONLY collision, when inverted); fades away as a
 * player approaches. Rendered by {@code DiaphanousBlockRenderer}.
 */
public class DiaphanousBlock extends BaseEntityBlock {
    private static final VoxelShape EMPTY_SHAPE = Shapes.empty();

    public DiaphanousBlock(Properties properties) {
        super(properties
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiaphanousBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanous) {
            return diaphanous.isInverted() ? Shapes.block() : EMPTY_SHAPE;
        }
        return EMPTY_SHAPE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Selectable only while the player is holding a diaphanous block (so it can be broken/edited).
        if (context instanceof EntityCollisionContext entityContext
            && entityContext.getEntity() instanceof Player player) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = player.getItemInHand(hand);
                if (!held.isEmpty() && held.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    return Shapes.block();
                }
            }
        }
        return EMPTY_SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        ResourceLocation blockId = RTNbt.getResourceLocation(stack, RTDataKeys.DIAPHANOUS_BLOCK_STATE);
        if (blockId != null && level.getBlockEntity(pos) instanceof DiaphanousBlockEntity diaphanous) {
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            diaphanous.setDisplayState(block != Blocks.AIR ? block.defaultBlockState() : Blocks.STONE.defaultBlockState());
            diaphanous.setInverted(RTNbt.getBoolean(stack, RTDataKeys.DIAPHANOUS_INVERTED));
        }
        if (!level.isClientSide) {
            neighborChanged(state, level, pos, this, pos, false);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockEntity(pos) instanceof DiaphanousBlockEntity diaphanous) {
            diaphanous.updateRenderMap();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DiaphanousBlockEntity diaphanous) {
            BlockState displayState = diaphanous.getDisplayState();
            if (displayState != null && displayState.getBlock() != Blocks.AIR) {
                displayState.getBlock().animateTick(displayState, level, pos, random);
            }
        }
    }
}
