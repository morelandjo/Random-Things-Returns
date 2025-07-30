package lumien.randomthings.datagen;

import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.worldgen.ModFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModWorldGenProvider::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, ModWorldGenProvider::bootstrapPlacedFeatures);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(ModConstants.MOD_ID));
    }

    private static void bootstrapConfiguredFeatures(net.minecraft.data.worldgen.BootstrapContext<ConfiguredFeature<?, ?>> context) {
        FeatureUtils.register(context, createKey("blood_roses"), ModFeatures.BLOOD_ROSES.get(),
                new net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration());
    }

    private static void bootstrapPlacedFeatures(net.minecraft.data.worldgen.BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(createPlacedKey("blood_roses"), new PlacedFeature(
                configuredFeatures.getOrThrow(createKey("blood_roses")),
                List.of(
                        RarityFilter.onAverageOnceEvery(32),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(net.minecraft.world.level.levelgen.VerticalAnchor.absolute(0), net.minecraft.world.level.levelgen.VerticalAnchor.absolute(128)),
                        BiomeFilter.biome()
                )
        ));
    }

    private static net.minecraft.resources.ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return net.minecraft.resources.ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, name));
    }

    private static net.minecraft.resources.ResourceKey<PlacedFeature> createPlacedKey(String name) {
        return net.minecraft.resources.ResourceKey.create(Registries.PLACED_FEATURE, 
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, name));
    }
}