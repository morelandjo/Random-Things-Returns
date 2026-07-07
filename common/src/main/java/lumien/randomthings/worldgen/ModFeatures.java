package lumien.randomthings.worldgen;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.AncientFurnaceConversion;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** Worldgen feature registry + Architectury biome modifications (replaces NeoForge biome-modifier JSONs). */
public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<AncientFurnaceFeature> ANCIENT_FURNACE =
        FEATURES.register("ancient_furnace", () -> new AncientFurnaceFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistrySupplier<NatureCoreFeature> NATURE_CORES =
        FEATURES.register("nature_cores", () -> new NatureCoreFeature(NoneFeatureConfiguration.CODEC));

    private static final ResourceKey<PlacedFeature> ANCIENT_FURNACE_PLACED =
        ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ModConstants.MOD_ID, "ancient_furnace"));
    private static final ResourceKey<PlacedFeature> NATURE_CORE_PLACED =
        ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ModConstants.MOD_ID, "nature_core_patch"));

    private ModFeatures() {
    }

    public static void register() {
        FEATURES.register();

        // Add the ancient furnace to all cold (convertible) biomes
        dev.architectury.registry.level.biome.BiomeModifications.addProperties(
            ctx -> ctx.getKey()
                .map(key -> AncientFurnaceConversion.coldBiomes().stream()
                    .anyMatch(biomeKey -> biomeKey.location().equals(key)))
                .orElse(false),
            (ctx, mutable) -> mutable.getGenerationProperties()
                .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ANCIENT_FURNACE_PLACED));

        // Nature cores across the whole overworld
        dev.architectury.registry.level.biome.BiomeModifications.addProperties(
            ctx -> ctx.hasTag(net.minecraft.tags.BiomeTags.IS_OVERWORLD),
            (ctx, mutable) -> mutable.getGenerationProperties()
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NATURE_CORE_PLACED));
    }
}
