package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Rare 3x3x3 jungle-wood shrine with a Nature Core at its heart, plus a nearby loot chest.
 * The NeoForge biome-condition tags of the 1.21.1 source are approximated with vanilla tags.
 */
public class NatureCoreFeature extends Feature<NoneFeatureConfiguration> {

    public static final ResourceLocation PLANT_CHEST_LOOT_TABLE =
        new ResourceLocation(ModConstants.MOD_ID, "chests/plant_chest");

    public NatureCoreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        // Biome-based spawn rate (vanilla-tag approximation of the source's NeoForge tags)
        int natureMult = 30;
        var biome = level.getBiome(pos);

        if (biome.is(BiomeTags.IS_JUNGLE)) {
            natureMult -= 8; // dense vegetation
        }
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            natureMult += 4; // sparse vegetation
        }
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
            natureMult -= 4; // wet
        }
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            natureMult += 2; // dry
        }
        if (biome.is(Biomes.DESERT)) {
            natureMult += 10; // dead
        }

        // 1 in (18 * natureMult) chance on top of the placed-feature rarity
        if (random.nextInt(18 * natureMult) != 0) {
            return false;
        }

        // Find suitable ground position
        int x = pos.getX() + random.nextInt(16);
        int z = pos.getZ() + random.nextInt(16);

        BlockPos groundPos = null;
        for (int y = level.getMaxBuildHeight() - 4; y >= level.getMinBuildHeight(); y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (level.getBlockState(checkPos).isSolid() && level.isEmptyBlock(checkPos.above())) {
                groundPos = checkPos;
                break;
            }
        }

        if (groundPos == null || !canPlaceStructure(level, groundPos)) {
            return false;
        }

        buildNatureCoreStructure(level, groundPos.above());
        placePlantChest(level, groundPos.above(), random);

        return true;
    }

    private boolean canPlaceStructure(WorldGenLevel level, BlockPos groundPos) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    if (!level.isEmptyBlock(groundPos.offset(dx, dy, dz))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void buildNatureCoreStructure(WorldGenLevel level, BlockPos basePos) {
        BlockState jungleLog = Blocks.JUNGLE_LOG.defaultBlockState();
        BlockState jungleLeaves = Blocks.JUNGLE_LEAVES.defaultBlockState();
        BlockState natureCore = ModBlocks.NATURE_CORE.get().defaultBlockState();

        // 3x3x3 checkerboard of logs/leaves with the core at the middle layer's center
        for (int layer = 0; layer <= 2; layer++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockState state;
                    if (layer == 1 && dx == 0 && dz == 0) {
                        state = natureCore;
                    } else {
                        state = ((dx + dz + 2) % 2) == 0 ? jungleLog : jungleLeaves;
                    }
                    setBlockIfEmpty(level, basePos.offset(dx, layer, dz), state);
                }
            }
        }
    }

    private void setBlockIfEmpty(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void placePlantChest(WorldGenLevel level, BlockPos basePos, RandomSource random) {
        for (int attempts = 0; attempts < 20; attempts++) {
            int dx = random.nextInt(11) - 5;
            int dz = random.nextInt(11) - 5;

            BlockPos chestPos = basePos.offset(dx, 0, dz);

            for (int dy = -2; dy <= 2; dy++) {
                BlockPos checkPos = chestPos.offset(0, dy, 0);

                if (level.getBlockState(checkPos.below()).isSolid() && level.isEmptyBlock(checkPos)) {
                    level.setBlock(checkPos, ModBlocks.PLANT_CHEST.get().defaultBlockState(), 2);

                    if (level.getBlockEntity(checkPos) instanceof RandomizableContainerBlockEntity chestEntity) {
                        chestEntity.setLootTable(PLANT_CHEST_LOOT_TABLE, random.nextLong());
                    }
                    return;
                }
            }
        }
    }
}
