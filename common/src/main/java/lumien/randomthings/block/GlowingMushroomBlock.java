package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A cave mushroom that glows and slowly spreads in the dark. */
public class GlowingMushroomBlock extends BushBlock {
    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public GlowingMushroomBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .lightLevel(state -> 15)
            .sound(SoundType.GRASS)
            .strength(0.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
            || state.is(BlockTags.STONE_ORE_REPLACEABLES)
            || state.is(BlockTags.BASE_STONE_OVERWORLD)
            || state.is(BlockTags.SAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        if (!this.mayPlaceOn(level.getBlockState(below), level, below)) {
            return false;
        }
        return !level.canSeeSky(pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        if (random.nextInt(20) == 0) {
            int nearbyMushrooms = 0;
            int searchRadius = 4;
            for (BlockPos checkPos : BlockPos.betweenClosed(
                pos.offset(-searchRadius, -1, -searchRadius), pos.offset(searchRadius, 1, searchRadius))) {
                if (level.getBlockState(checkPos).is(this)) {
                    nearbyMushrooms++;
                    if (nearbyMushrooms >= 5) {
                        return;
                    }
                }
            }
            BlockPos targetPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            for (int i = 0; i < 4; i++) {
                if (level.isEmptyBlock(targetPos) && this.canSurvive(this.defaultBlockState(), level, targetPos)) {
                    level.setBlock(targetPos, this.defaultBlockState(), 2);
                    return;
                }
                targetPos = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }
        }
    }
}
