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
}