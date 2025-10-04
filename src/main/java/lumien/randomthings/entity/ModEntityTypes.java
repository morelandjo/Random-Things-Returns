package lumien.randomthings.entity;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ModConstants.MOD_ID);

    public static final Supplier<EntityType<EclipsedClockEntity>> ECLIPSED_CLOCK = ENTITY_TYPES.register("eclipsed_clock", 
        () -> EntityType.Builder.<EclipsedClockEntity>of(EclipsedClockEntity::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .build("eclipsed_clock")
    );
    
    public static final Supplier<EntityType<TimeAcceleratorEntity>> TIME_ACCELERATOR = ENTITY_TYPES.register("time_accelerator", 
        () -> EntityType.Builder.<TimeAcceleratorEntity>of(TimeAcceleratorEntity::new, MobCategory.MISC)
            .sized(0.1f, 0.1f)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build("time_accelerator")
    );
    
    public static final Supplier<EntityType<SpiritEntity>> SPIRIT = ENTITY_TYPES.register("spirit",
        () -> EntityType.Builder.<SpiritEntity>of(SpiritEntity::new, MobCategory.MONSTER)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("spirit")
    );

    public static final Supplier<EntityType<StableEnderPearlEntity>> STABLE_ENDER_PEARL = ENTITY_TYPES.register("stable_ender_pearl",
        () -> EntityType.Builder.<StableEnderPearlEntity>of(StableEnderPearlEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(6)
            .updateInterval(20)
            .build("stable_ender_pearl")
    );

    public static final Supplier<EntityType<GoldenEggEntity>> GOLDEN_EGG = ENTITY_TYPES.register("golden_egg",
        () -> EntityType.Builder.<GoldenEggEntity>of(GoldenEggEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(64)
            .updateInterval(10)
            .build("golden_egg")
    );

    public static final Supplier<EntityType<GoldenChickenEntity>> GOLDEN_CHICKEN = ENTITY_TYPES.register("golden_chicken",
        () -> EntityType.Builder.<GoldenChickenEntity>of(GoldenChickenEntity::new, MobCategory.CREATURE)
            .sized(0.4f, 0.7f)
            .clientTrackingRange(80)
            .updateInterval(3)
            .build("golden_chicken")
    );
}