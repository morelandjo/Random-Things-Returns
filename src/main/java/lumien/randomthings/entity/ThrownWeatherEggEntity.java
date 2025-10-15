package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.WeatherEggItem;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownWeatherEggEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Integer> EGG_TYPE = SynchedEntityData.defineId(ThrownWeatherEggEntity.class, EntityDataSerializers.INT);

    public ThrownWeatherEggEntity(EntityType<? extends ThrownWeatherEggEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownWeatherEggEntity(Level level, LivingEntity shooter, WeatherEggItem.WeatherType weatherType) {
        super(ModEntityTypes.THROWN_WEATHER_EGG.get(), shooter, level);
        this.entityData.set(EGG_TYPE, weatherType.ordinal());
    }

    public ThrownWeatherEggEntity(Level level, double x, double y, double z, WeatherEggItem.WeatherType weatherType) {
        super(ModEntityTypes.THROWN_WEATHER_EGG.get(), x, y, z, level);
        this.entityData.set(EGG_TYPE, weatherType.ordinal());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EGG_TYPE, 0);
    }

    @Override
    protected Item getDefaultItem() {
        // Check if entityData is initialized to avoid NPE during entity creation
        if (this.entityData == null) {
            return ModItems.WEATHER_EGG_SUN.get(); // Default fallback
        }

        // Return the appropriate weather egg based on type
        WeatherEggItem.WeatherType type = getWeatherType();
        return switch (type) {
            case SUN -> ModItems.WEATHER_EGG_SUN.get();
            case RAIN -> ModItems.WEATHER_EGG_RAIN.get();
            case STORM -> ModItems.WEATHER_EGG_STORM.get();
        };
    }

    public WeatherEggItem.WeatherType getWeatherType() {
        // Safety check for entityData
        if (this.entityData == null) {
            return WeatherEggItem.WeatherType.SUN;
        }

        int typeOrdinal = this.entityData.get(EGG_TYPE);
        WeatherEggItem.WeatherType[] types = WeatherEggItem.WeatherType.values();
        if (typeOrdinal >= 0 && typeOrdinal < types.length) {
            return types[typeOrdinal];
        }
        return WeatherEggItem.WeatherType.SUN;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // Spawn egg break particles
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(this.getDefaultItem())),
                    this.getX(), this.getY(), this.getZ(),
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                    ((double) this.random.nextFloat() - 0.5D) * 0.08D
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Deal 1 damage to hit entity
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            // Spawn weather cloud at impact location
            WeatherCloudEntity weatherCloud = new WeatherCloudEntity(
                this.level(),
                result.getLocation().x,
                result.getLocation().y + 0.5,
                result.getLocation().z,
                getWeatherType()
            );
            this.level().addFreshEntity(weatherCloud);

            // Notify clients to spawn break particles
            this.level().broadcastEntityEvent(this, (byte) 3);

            // Remove the thrown egg entity
            this.discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("EggType", this.entityData.get(EGG_TYPE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(EGG_TYPE, compound.getInt("EggType"));
    }
}
