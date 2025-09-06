package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class PitcherPlantFeature extends Feature<NoneFeatureConfiguration> {

    public PitcherPlantFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        
        // Only place in warm biomes (temperature >= 0.8F)
        Biome biome = level.getBiome(pos).value();
        if (biome.getBaseTemperature() < 0.8F) {
            return false;
        }
        
        // 10% chance to spawn, matching original implementation
        if (random.nextInt(10) != 0) {
            return false;
        }
        
        BlockState pitcherPlantState = ModBlocks.PITCHER_PLANT.get().defaultBlockState();
        int placed = 0;

        // Try to place 1-3 pitcher plants in the area
        int attempts = 1 + random.nextInt(3);
        
        for (int j = 0; j < attempts; ++j) {
            BlockPos blockpos = pos.offset(
                random.nextInt(8) - random.nextInt(8), 
                random.nextInt(2) - random.nextInt(2), 
                random.nextInt(8) - random.nextInt(8)
            );
            
            if (level.isEmptyBlock(blockpos) && 
                blockpos.getY() < level.getMaxBuildHeight() && 
                pitcherPlantState.canSurvive(level, blockpos)) {
                
                level.setBlock(blockpos, pitcherPlantState, 2);
                ++placed;
            }
        }

        return placed > 0;
    }
}