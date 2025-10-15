package lumien.randomthings.lib;

import java.util.HashMap;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Maps cold biomes to their warmer equivalents for Ancient Furnace transformation
 */
public class AncientFurnaceConversion {
    private static final HashMap<ResourceKey<Biome>, ResourceKey<Biome>> conversionMap = new HashMap<>();

    static {
        // Cold to temperate biome conversions
        conversionMap.put(Biomes.TAIGA, Biomes.FOREST);
        conversionMap.put(Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_BIRCH_FOREST);
        conversionMap.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, Biomes.OLD_GROWTH_BIRCH_FOREST);
        conversionMap.put(Biomes.SNOWY_TAIGA, Biomes.BIRCH_FOREST);

        // Frozen ocean/river conversions
        conversionMap.put(Biomes.FROZEN_OCEAN, Biomes.OCEAN);
        conversionMap.put(Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_OCEAN);
        conversionMap.put(Biomes.FROZEN_RIVER, Biomes.RIVER);

        // Ice plains/tundra conversions
        conversionMap.put(Biomes.SNOWY_PLAINS, Biomes.PLAINS);
        conversionMap.put(Biomes.ICE_SPIKES, Biomes.PLAINS);
        conversionMap.put(Biomes.SNOWY_SLOPES, Biomes.MEADOW);
        conversionMap.put(Biomes.SNOWY_BEACH, Biomes.BEACH);

        // Grove conversion (cold mountain)
        conversionMap.put(Biomes.GROVE, Biomes.FOREST);

        // Jagged peaks conversion
        conversionMap.put(Biomes.JAGGED_PEAKS, Biomes.STONY_PEAKS);
        conversionMap.put(Biomes.FROZEN_PEAKS, Biomes.STONY_PEAKS);
    }

    /**
     * Get the warmer biome variant for the given cold biome
     * @param biome The ResourceKey of the current biome
     * @return The ResourceKey of the warmer biome, or null if no conversion exists
     */
    public static ResourceKey<Biome> getHeatingConversion(ResourceKey<Biome> biome) {
        return conversionMap.get(biome);
    }
}
