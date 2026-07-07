package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A bouncier slime block; compress it further with a shovel for a stronger bounce and lower profile. */
public class CompressedSlimeBlock extends Block {
    public static final IntegerProperty COMPRESSION = IntegerProperty.create("compression", 0, 2);
    private static final VoxelShape[] SHAPES = {
        Block.box(0, 0, 0, 16, 8, 16), Block.box(0, 0, 0, 16, 4, 16), Block.box(0, 0, 0, 16, 2, 16)
    };

    public CompressedSlimeBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.5F).sound(SoundType.SLIME_BLOCK).friction(0.8F).jumpFactor(1.0F).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(COMPRESSION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPRESSION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(COMPRESSION)];
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() instanceof ShovelItem && !level.isClientSide) {
            int compression = state.getValue(COMPRESSION);
            if (compression < 2) {
                level.setBlock(pos, state.setValue(COMPRESSION, compression + 1), 3);
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F + compression * 0.1F);
                if (!player.isCreative()) {
                    heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.SLIME_BLOCK);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.getDeltaMovement().y < 1.0) {
            int compression = state.getValue(COMPRESSION);
            entity.fallDistance = 0;
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0.8 + compression * 0.4, entity.getDeltaMovement().z);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }
}
