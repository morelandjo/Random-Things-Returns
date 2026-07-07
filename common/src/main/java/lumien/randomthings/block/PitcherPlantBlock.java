package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A plant that produces water: right-click with a bottle or bucket to fill it, and it tops up
 * adjacent cauldrons. (The 1.21.1 build also filled arbitrary Forge fluid containers/tanks; that
 * capability path has no cross-loader equivalent and is omitted.)
 */
public class PitcherPlantBlock extends BushBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 15.0D, 11.0D);

    public PitcherPlantBlock() {
        super(BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.CROP).randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide) {
                ItemStack waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                giveResult(player, hand, heldItem, waterBottle);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (heldItem.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                giveResult(player, hand, heldItem, new ItemStack(Items.WATER_BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private void giveResult(Player player, InteractionHand hand, ItemStack heldItem, ItemStack result) {
        if (!player.getAbilities().instabuild) {
            heldItem.shrink(1);
        }
        if (heldItem.isEmpty()) {
            player.setItemInHand(hand, result);
        } else if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.getBlock() instanceof LayeredCauldronBlock) {
                int currentLevel = adjacentState.getValue(LayeredCauldronBlock.LEVEL);
                if (currentLevel < 3) {
                    level.setBlock(adjacentPos, adjacentState.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1), 3);
                    level.playSound(null, adjacentPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
                }
            } else if (adjacentState.is(Blocks.CAULDRON)) {
                level.setBlock(adjacentPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1), 3);
                level.playSound(null, adjacentPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }
}
