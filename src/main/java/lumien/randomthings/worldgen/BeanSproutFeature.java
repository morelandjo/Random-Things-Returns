package lumien.randomthings.worldgen;

import lumien.randomthings.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BeanSproutFeature extends Feature<NoneFeatureConfiguration> {
    
    public BeanSproutFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        
        // 50% chance per chunk like original mod
        if (!random.nextBoolean()) {
            return false;
        }
        
        // Random position within chunk (matching original logic)
        int x = pos.getX() + random.nextInt(16);
        int z = pos.getZ() + random.nextInt(16);
        
        // Find top solid block starting from Y=40 (matching original)
        BlockPos groundPos = null;
        for (int y = 40; y < level.getMaxBuildHeight() - 1; y++) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (level.getBlockState(checkPos).isSolid() && level.isEmptyBlock(checkPos.above())) {
                groundPos = checkPos;
                break;
            }
        }
        
        if (groundPos == null) {
            return false;
        }
        
        BlockPos plantPos = groundPos.above();
        BlockState beanSproutState = ModBlocks.BEANSPROUT.get().defaultBlockState();
        
        // Check if bean sprout can survive (light level >= 8)
        if (level.getRawBrightness(plantPos, 0) >= 8) {
            // Check if the ground is suitable (replicate BlockBeanSprout.mayPlaceOn logic)
            BlockState belowState = level.getBlockState(groundPos);
            boolean canPlant = belowState.is(net.minecraft.tags.BlockTags.DIRT) || 
                             belowState.getBlock() == net.minecraft.world.level.block.Blocks.GRASS_BLOCK ||
                             belowState.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND;
            
            if (canPlant) {
                // Set random age (0-7) like in original mod
                int age = random.nextInt(8);
                level.setBlock(plantPos, beanSproutState.setValue(lumien.randomthings.block.BlockBeanSprout.AGE, age), 2);
                return true;
            }
        }
        
        return false;
    }
}