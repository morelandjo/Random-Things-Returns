package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowingMushroomBlock extends BushBlock {
    public static final MapCodec<GlowingMushroomBlock> CODEC = simpleCodec(properties -> new GlowingMushroomBlock());

    @Override
    public MapCodec<GlowingMushroomBlock> codec() {
        return CODEC;
    }

    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public GlowingMushroomBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .lightLevel((state) -> 15)
                .sound(net.minecraft.world.level.block.SoundType.GRASS)
                .strength(0.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) ||
               state.is(BlockTags.STONE_ORE_REPLACEABLES) ||
               state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) ||
               state.is(BlockTags.SAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        // Check if can place on the block below
        if (!this.mayPlaceOn(belowState, level, below)) {
            return false;
        }

        // Must not be able to see sky (cave requirement)
        return !level.canSeeSky(pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Check if should drop
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        // Spreading logic - 5% chance to attempt spread
        if (random.nextInt(20) == 0) {
            int nearbyMushrooms = 0;
            int maxNearby = 5;
            int searchRadius = 4;

            // Count nearby mushrooms
            for (BlockPos checkPos : BlockPos.betweenClosed(
                    pos.offset(-searchRadius, -1, -searchRadius),
                    pos.offset(searchRadius, 1, searchRadius))) {
                if (level.getBlockState(checkPos).is(this)) {
                    nearbyMushrooms++;
                    if (nearbyMushrooms >= maxNearby) {
                        return; // Too crowded, don't spread
                    }
                }
            }

            // Try to spread to a nearby position
            BlockPos targetPos = pos.offset(
                    random.nextInt(3) - 1,
                    random.nextInt(2) - random.nextInt(2),
                    random.nextInt(3) - 1);

            // Attempt multiple times to find a valid position
            for (int i = 0; i < 4; i++) {
                if (level.isEmptyBlock(targetPos) && this.canSurvive(this.defaultBlockState(), level, targetPos)) {
                    level.setBlock(targetPos, this.defaultBlockState(), 2);
                    return;
                }

                targetPos = pos.offset(
                        random.nextInt(3) - 1,
                        random.nextInt(2) - random.nextInt(2),
                        random.nextInt(3) - 1);
            }
        }
    }
}
