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

public class BloodRoseFeature extends Feature<NoneFeatureConfiguration> {

    public BloodRoseFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        
        BlockState blockstate = ModBlocks.BLOOD_ROSE.get().defaultBlockState();
        int placed = 0;

        for (int j = 0; j < 8; ++j) {
            BlockPos blockpos = pos.offset(
                random.nextInt(8) - random.nextInt(8), 
                random.nextInt(4) - random.nextInt(4), 
                random.nextInt(8) - random.nextInt(8)
            );
            
            if (level.isEmptyBlock(blockpos) && 
                blockpos.getY() < level.getMaxBuildHeight() && 
                blockstate.canSurvive(level, blockpos)) {
                
                level.setBlock(blockpos, blockstate, 2);
                ++placed;
            }
        }

        return placed > 0;
    }
}