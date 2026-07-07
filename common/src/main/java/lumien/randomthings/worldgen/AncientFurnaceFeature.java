package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import lumien.randomthings.block.AncientBrickBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.AncientFurnaceConversion;
import lumien.randomthings.util.BlockPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * World generation for the Ancient Furnace 3x3x3 multiblock. Spawns rarely in cold biomes
 * (rarity is in the placed_feature JSON):
 * Y=0 output brick (center) + default bricks, Y=1 furnace core + rune bricks,
 * Y=2 star-empty brick (activation point) + default bricks.
 */
public class AncientFurnaceFeature extends Feature<NoneFeatureConfiguration> {

    public AncientFurnaceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        // Only spawn in biomes that have a heating conversion
        ResourceKey<Biome> biomeKey = level.getBiome(origin).unwrapKey().orElse(null);
        if (biomeKey == null || AncientFurnaceConversion.getHeatingConversion(biomeKey) == null) {
            return false;
        }

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        if (y < 5 || y > 200) {
            return false;
        }

        // Center the 3x3 structure on the origin column
        BlockPos placementPos = new BlockPos(origin.getX() - 1, y, origin.getZ() - 1);

        BlockPattern pattern = createAncientFurnacePattern();
        if (!pattern.canPlace(level, placementPos)) {
            return false;
        }

        pattern.place(level, placementPos);
        return true;
    }

    private BlockPattern createAncientFurnacePattern() {
        BlockPattern pattern = new BlockPattern(3, 3, 3);

        BlockState defaultBrick = ModBlocks.ANCIENT_BRICK.get().defaultBlockState()
            .setValue(AncientBrickBlock.VARIANT, AncientBrickBlock.Variant.DEFAULT);
        BlockState runeBrick = ModBlocks.ANCIENT_BRICK.get().defaultBlockState()
            .setValue(AncientBrickBlock.VARIANT, AncientBrickBlock.Variant.RUNES);
        BlockState starEmpty = ModBlocks.ANCIENT_BRICK.get().defaultBlockState()
            .setValue(AncientBrickBlock.VARIANT, AncientBrickBlock.Variant.STAR_EMPTY);
        BlockState outputBrick = ModBlocks.ANCIENT_BRICK.get().defaultBlockState()
            .setValue(AncientBrickBlock.VARIANT, AncientBrickBlock.Variant.OUTPUT);
        BlockState furnace = ModBlocks.ANCIENT_FURNACE.get().defaultBlockState();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                boolean center = x == 1 && z == 1;
                pattern.setBlockState(x, 0, z, center ? outputBrick : defaultBrick);
                pattern.setBlockState(x, 1, z, center ? furnace : runeBrick);
                pattern.setBlockState(x, 2, z, center ? starEmpty : defaultBrick);
            }
        }

        return pattern;
    }
}
