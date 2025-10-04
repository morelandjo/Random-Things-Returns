package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GlowingMushroomFeature extends Feature<NoneFeatureConfiguration> {

    public GlowingMushroomFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();

        BlockState mushroomState = ModBlocks.GLOWING_MUSHROOM.get().defaultBlockState();
        int placed = 0;

        // Try to place mushrooms in a patch (up to 64 attempts)
        for (int i = 0; i < 64; ++i) {
            BlockPos targetPos = pos.offset(
                random.nextInt(8) - random.nextInt(8),
                random.nextInt(4) - random.nextInt(4),
                random.nextInt(8) - random.nextInt(8)
            );

            // Must be empty and not able to see sky (cave requirement)
            if (level.isEmptyBlock(targetPos) &&
                !level.canSeeSky(targetPos) &&
                targetPos.getY() < level.getMaxBuildHeight() &&
                mushroomState.canSurvive(level, targetPos)) {

                level.setBlock(targetPos, mushroomState, 2);
                ++placed;
            }
        }

        return placed > 0;
    }
}
