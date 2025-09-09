package lumien.randomthings.worldgen;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModConstants.MOD_ID);

    public static final DeferredHolder<Feature<?>, BloodRoseFeature> BLOOD_ROSES = FEATURES.register("blood_roses", 
        () -> new BloodRoseFeature(NoneFeatureConfiguration.CODEC));
        
    public static final DeferredHolder<Feature<?>, LotusFeature> LOTUS_PLANTS = FEATURES.register("lotus_plants", 
        () -> new LotusFeature(NoneFeatureConfiguration.CODEC));
        
    public static final DeferredHolder<Feature<?>, PitcherPlantFeature> PITCHER_PLANTS = FEATURES.register("pitcher_plants", 
        () -> new PitcherPlantFeature(NoneFeatureConfiguration.CODEC));
        
    public static final DeferredHolder<Feature<?>, BeanSproutFeature> BEAN_SPROUTS = FEATURES.register("bean_sprouts", 
        () -> new BeanSproutFeature(NoneFeatureConfiguration.CODEC));
        
    public static final DeferredHolder<Feature<?>, NatureCoreFeature> NATURE_CORES = FEATURES.register("nature_cores", 
        () -> new NatureCoreFeature(NoneFeatureConfiguration.CODEC));
}