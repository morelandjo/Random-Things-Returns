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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * World generation feature for Ancient Furnace structures.
 * Spawns rarely in cold biomes at a rate of approximately 1/2000 chunks.
 * <p>
 * Structure is a 3×3×3 multi-block:
 * - Y=0: Output brick (center), default bricks (8 surrounding)
 * - Y=1: Ancient Furnace core (center), rune bricks (8 surrounding)
 * - Y=2: Star empty brick (center), default bricks (8 surrounding)
 */
public class AncientFurnaceFeature extends Feature<NoneFeatureConfiguration> {

    public AncientFurnaceFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        // Only spawn in cold biomes (biomes that have a heating conversion)
        ResourceKey<Biome> biomeKey = level.getBiome(origin).unwrapKey().orElse(null);
        if (biomeKey == null || AncientFurnaceConversion.getHeatingConversion(biomeKey) == null) {
            return false;
        }

        // Find a suitable Y position at world surface
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());

        // Ensure we're at a reasonable Y level (allow higher Y values for mountains)
        if (y < 5 || y > 200) {
            return false;
        }

        // Center the structure (offset by -1 in X and Z since it's 3×3)
        BlockPos placementPos = new BlockPos(origin.getX() - 1, y, origin.getZ() - 1);

        // Build the pattern
        BlockPattern pattern = createAncientFurnacePattern();

        // Check if we can place the pattern
        if (!pattern.canPlace(level, placementPos)) {
            return false;
        }

        // Place the structure
        pattern.place(level, placementPos);

        return true;
    }

    /**
     * Creates the 3×3×3 Ancient Furnace structure pattern.
     * @return The block pattern ready to be placed
     */
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

        // Layer 0 (Bottom): Output brick center, default bricks surrounding
        pattern.setBlockState(0, 0, 0, defaultBrick);
        pattern.setBlockState(1, 0, 0, defaultBrick);
        pattern.setBlockState(2, 0, 0, defaultBrick);
        pattern.setBlockState(0, 0, 1, defaultBrick);
        pattern.setBlockState(1, 0, 1, outputBrick);    // Center: output brick
        pattern.setBlockState(2, 0, 1, defaultBrick);
        pattern.setBlockState(0, 0, 2, defaultBrick);
        pattern.setBlockState(1, 0, 2, defaultBrick);
        pattern.setBlockState(2, 0, 2, defaultBrick);

        // Layer 1 (Middle): Ancient Furnace center, rune bricks surrounding
        pattern.setBlockState(0, 1, 0, runeBrick);
        pattern.setBlockState(1, 1, 0, runeBrick);
        pattern.setBlockState(2, 1, 0, runeBrick);
        pattern.setBlockState(0, 1, 1, runeBrick);
        pattern.setBlockState(1, 1, 1, furnace);        // Center: furnace core
        pattern.setBlockState(2, 1, 1, runeBrick);
        pattern.setBlockState(0, 1, 2, runeBrick);
        pattern.setBlockState(1, 1, 2, runeBrick);
        pattern.setBlockState(2, 1, 2, runeBrick);

        // Layer 2 (Top): Star empty center, default bricks surrounding
        pattern.setBlockState(0, 2, 0, defaultBrick);
        pattern.setBlockState(1, 2, 0, defaultBrick);
        pattern.setBlockState(2, 2, 0, defaultBrick);
        pattern.setBlockState(0, 2, 1, defaultBrick);
        pattern.setBlockState(1, 2, 1, starEmpty);      // Center: star empty (activation point)
        pattern.setBlockState(2, 2, 1, defaultBrick);
        pattern.setBlockState(0, 2, 2, defaultBrick);
        pattern.setBlockState(1, 2, 2, defaultBrick);
        pattern.setBlockState(2, 2, 2, defaultBrick);

        return pattern;
    }
}
