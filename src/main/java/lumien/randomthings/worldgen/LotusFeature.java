package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import lumien.randomthings.block.LotusBlock;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LotusFeature extends Feature<NoneFeatureConfiguration> {

    public LotusFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        
        BlockState lotusState = ModBlocks.LOTUS.get().defaultBlockState();
        int placed = 0;

        // Try to place 3-6 lotus plants in the area
        int attempts = 3 + random.nextInt(4);
        
        for (int j = 0; j < attempts; ++j) {
            BlockPos blockpos = pos.offset(
                random.nextInt(8) - random.nextInt(8), 
                random.nextInt(2) - random.nextInt(2), 
                random.nextInt(8) - random.nextInt(8)
            );
            
            if (level.isEmptyBlock(blockpos) && 
                blockpos.getY() < level.getMaxBuildHeight() && 
                lotusState.canSurvive(level, blockpos)) {
                
                // Place lotus at a random growth stage (0-3)
                int age = random.nextInt(4);
                BlockState ageState = lotusState.setValue(LotusBlock.AGE, age);
                
                level.setBlock(blockpos, ageState, 2);
                ++placed;
            }
        }

        return placed > 0;
    }
}