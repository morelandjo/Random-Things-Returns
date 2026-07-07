package lumien.randomthings.entity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Entity type registry.
 */
public final class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<TemporaryFlooFireplaceEntity>> TEMPORARY_FLOO_FIREPLACE =
        ENTITY_TYPES.register("temporary_floo_fireplace",
            () -> EntityType.Builder.<TemporaryFlooFireplaceEntity>of(TemporaryFlooFireplaceEntity::new, MobCategory.MISC)
                .sized(1.0F, 1.0F)
                .clientTrackingRange(10)
                .build("temporary_floo_fireplace"));

    public static final RegistrySupplier<EntityType<ThrownWeatherEggEntity>> THROWN_WEATHER_EGG =
        ENTITY_TYPES.register("thrown_weather_egg",
            () -> EntityType.Builder.<ThrownWeatherEggEntity>of(ThrownWeatherEggEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build("thrown_weather_egg"));

    public static final RegistrySupplier<EntityType<WeatherCloudEntity>> WEATHER_CLOUD =
        ENTITY_TYPES.register("weather_cloud",
            () -> EntityType.Builder.<WeatherCloudEntity>of(WeatherCloudEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(10)
                .build("weather_cloud"));

    public static final RegistrySupplier<EntityType<ArtificialEndPortalEntity>> ARTIFICIAL_END_PORTAL =
        ENTITY_TYPES.register("artificial_end_portal",
            () -> EntityType.Builder.<ArtificialEndPortalEntity>of(ArtificialEndPortalEntity::new, MobCategory.MISC)
                .sized(3.0F, 1.0F)
                .clientTrackingRange(10)
                .build("artificial_end_portal"));

    public static final RegistrySupplier<EntityType<SpectreIlluminatorEntity>> SPECTRE_ILLUMINATOR =
        ENTITY_TYPES.register("spectre_illuminator",
            () -> EntityType.Builder.<SpectreIlluminatorEntity>of(SpectreIlluminatorEntity::new, MobCategory.MISC)
                .sized(0.5F, 0.5F)
                .clientTrackingRange(10)
                .build("spectre_illuminator"));

    public static final RegistrySupplier<EntityType<TimeAcceleratorEntity>> TIME_ACCELERATOR =
        ENTITY_TYPES.register("time_accelerator",
            () -> EntityType.Builder.<TimeAcceleratorEntity>of(TimeAcceleratorEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(8)
                .build("time_accelerator"));

    public static final RegistrySupplier<EntityType<GoldenEggEntity>> GOLDEN_EGG =
        ENTITY_TYPES.register("golden_egg",
            () -> EntityType.Builder.<GoldenEggEntity>of(GoldenEggEntity::new, MobCategory.MISC)
                .sized(0.25F, 0.25F)
                .clientTrackingRange(64)
                .updateInterval(10)
                .build("golden_egg"));

    public static final RegistrySupplier<EntityType<GoldenChickenEntity>> GOLDEN_CHICKEN =
        ENTITY_TYPES.register("golden_chicken",
            () -> EntityType.Builder.<GoldenChickenEntity>of(GoldenChickenEntity::new, MobCategory.CREATURE)
                .sized(0.4F, 0.7F)
                .clientTrackingRange(80)
                .updateInterval(3)
                .build("golden_chicken"));

    public static final RegistrySupplier<EntityType<EclipsedClockEntity>> ECLIPSED_CLOCK =
        ENTITY_TYPES.register("eclipsed_clock",
            () -> EntityType.Builder.<EclipsedClockEntity>of(EclipsedClockEntity::new, MobCategory.MISC)
                .sized(0.375F, 0.375F)
                .clientTrackingRange(10)
                .build("eclipsed_clock"));

    private ModEntityTypes() {
    }

    public static void register() {
        ENTITY_TYPES.register();
        dev.architectury.registry.level.entity.EntityAttributeRegistry.register(
            GOLDEN_CHICKEN, GoldenChickenEntity::createAttributes);
    }
}
