package lumien.randomthings.worldgen;

import lumien.randomthings.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.common.Tags;

public class NatureCoreFeature extends Feature<NoneFeatureConfiguration> {
    
    public NatureCoreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        
        // Calculate biome-based spawn rate (based on original mod logic)
        int natureMult = 30; // Base multiplier
        var biome = level.getBiome(pos);
        
        // Apply biome modifiers based on biome tags
        if (biome.is(Tags.Biomes.IS_DENSE_VEGETATION)) {
            natureMult -= 8; // More likely in dense biomes
        }
        if (biome.is(Tags.Biomes.IS_SPARSE_VEGETATION)) {
            natureMult += 4; // Less likely in sparse biomes
        }
        if (biome.is(Tags.Biomes.IS_WET)) {
            natureMult -= 4; // More likely in wet biomes
        }
        if (biome.is(Tags.Biomes.IS_DRY)) {
            natureMult += 2; // Less likely in dry biomes
        }
        if (biome.is(Tags.Biomes.IS_DEAD)) {
            natureMult += 10; // Much less likely in dead biomes
        }
        if (biome.is(Tags.Biomes.IS_MAGICAL)) {
            natureMult -= 8; // More likely in magical biomes
        }
        
        // Apply spawn rate: 1 in (18 * natureMult) chance
        int spawnChance = 18 * natureMult;
        if (random.nextInt(spawnChance) != 0) {
            return false;
        }
        
        // Find suitable ground position
        int x = pos.getX() + random.nextInt(16);
        int z = pos.getZ() + random.nextInt(16);
        
        BlockPos groundPos = null;
        for (int y = level.getMaxBuildHeight() - 4; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            BlockPos abovePos = checkPos.above();
            
            if (level.getBlockState(checkPos).isSolid() && 
                level.isEmptyBlock(abovePos)) {
                groundPos = checkPos;
                break;
            }
        }
        
        if (groundPos == null) {
            return false;
        }
        
        // Check if 5x5x3 area is clear for structure
        if (!canPlaceStructure(level, groundPos)) {
            return false;
        }
        
        // Build the Nature Core structure (3x3x3)
        buildNatureCoreStructure(level, groundPos.above(), random);
        
        // Place Plant Chest within 5-block radius
        placePlantChest(level, groundPos.above(), random);
        
        return true;
    }
    
    private boolean canPlaceStructure(WorldGenLevel level, BlockPos groundPos) {
        // Check 5x5x3 area is clear (original mod logic)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    BlockPos checkPos = groundPos.offset(dx, dy, dz);
                    if (!level.isEmptyBlock(checkPos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void buildNatureCoreStructure(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        BlockState jungleLog = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState jungleLeaves = Blocks.JUNGLE_LEAVES.defaultBlockState();
        BlockState natureCore = ModBlocks.NATURE_CORE.get().defaultBlockState();
        
        // Layer 0 (Ground level)
        setBlockIfEmpty(level, basePos.offset(-1, 0, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 0, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 0, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 0,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 0,  0), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 1, 0,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 0,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 0,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 0,  1), jungleLog);
        
        // Layer 1 (Middle - Nature Core)
        setBlockIfEmpty(level, basePos.offset(-1, 1, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 1, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 1, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 1,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 1,  0), natureCore); // Nature Core in center
        setBlockIfEmpty(level, basePos.offset( 1, 1,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 1,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 1,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 1,  1), jungleLog);
        
        // Layer 2 (Top)
        setBlockIfEmpty(level, basePos.offset(-1, 2, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 2, -1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 2, -1), jungleLog);
        setBlockIfEmpty(level, basePos.offset(-1, 2,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 0, 2,  0), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 1, 2,  0), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset(-1, 2,  1), jungleLog);
        setBlockIfEmpty(level, basePos.offset( 0, 2,  1), jungleLeaves);
        setBlockIfEmpty(level, basePos.offset( 1, 2,  1), jungleLog);
    }
    
    private void setBlockIfEmpty(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
    
    private void placePlantChest(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        // Try to place Plant Chest within 5-block radius (11x11 area)
        for (int attempts = 0; attempts < 20; attempts++) {
            int dx = random.nextInt(11) - 5; // -5 to +5
            int dz = random.nextInt(11) - 5; // -5 to +5
            
            BlockPos chestPos = basePos.offset(dx, 0, dz);
            
            // Find ground level
            for (int dy = -2; dy <= 2; dy++) {
                BlockPos checkPos = chestPos.offset(0, dy, 0);
                BlockPos belowPos = checkPos.below();
                
                if (level.getBlockState(belowPos).isSolid() && level.isEmptyBlock(checkPos)) {
                    // Place Plant Chest
                    level.setBlock(checkPos, ModBlocks.PLANT_CHEST.get().defaultBlockState(), 2);
                    
                    // Set loot table for the chest
                    var blockEntity = level.getBlockEntity(checkPos);
                    if (blockEntity instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chestEntity) {
                        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTableKey = 
                            ResourceKey.create(Registries.LOOT_TABLE, lumien.randomthings.block.PlantChestBlock.PLANT_CHEST_LOOT_TABLE);
                        chestEntity.setLootTable(lootTableKey, random.nextLong());
                    }
                    return;
                }
            }
        }
    }
}