package lumien.randomthings.entity;

import lumien.randomthings.item.WeatherEggItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

/** Rises from a thrown weather egg's impact; on reaching the sky it sets the world's weather. */
public class WeatherCloudEntity extends Entity {
    private static final EntityDataAccessor<Integer> EGG_TYPE = SynchedEntityData.defineId(WeatherCloudEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(WeatherCloudEntity.class, EntityDataSerializers.INT);

    public WeatherCloudEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public WeatherCloudEntity(Level level, double x, double y, double z, WeatherEggItem.WeatherType weatherType) {
        super(ModEntityTypes.WEATHER_CLOUD.get(), level);
        this.noPhysics = true;
        this.entityData.set(EGG_TYPE, weatherType.ordinal());
        this.entityData.set(AGE, 0);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(EGG_TYPE, 0);
        this.entityData.define(AGE, 0);
    }

    public WeatherEggItem.WeatherType getWeatherType() {
        int ordinal = this.entityData.get(EGG_TYPE);
        WeatherEggItem.WeatherType[] types = WeatherEggItem.WeatherType.values();
        return (ordinal >= 0 && ordinal < types.length) ? types[ordinal] : WeatherEggItem.WeatherType.SUN;
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    @Override
    public void tick() {
        super.tick();
        int age = this.entityData.get(AGE);
        if (age < 200) {
            this.setDeltaMovement(0, 0.007, 0);
        } else if (this.getY() < this.level().getHeight()) {
            double motionY = this.getDeltaMovement().y;
            motionY += 0.001;
            motionY *= 1.02;
            this.setDeltaMovement(0, motionY, 0);
        } else if (!this.level().isClientSide) {
            changeWeather();
            this.discard();
            return;
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.level().isClientSide) {
            spawnParticles();
        }
        this.entityData.set(AGE, age + 1);
    }

    private void changeWeather() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ServerLevelData levelData = (ServerLevelData) serverLevel.getLevelData();
            int duration = (300 + this.random.nextInt(600)) * 20; // 5-15 minutes
            switch (getWeatherType()) {
                case SUN -> {
                    levelData.setRainTime(0);
                    levelData.setThunderTime(0);
                    levelData.setClearWeatherTime(duration);
                    levelData.setRaining(false);
                    levelData.setThundering(false);
                }
                case RAIN -> {
                    levelData.setClearWeatherTime(0);
                    levelData.setRainTime(duration);
                    levelData.setThunderTime(duration);
                    levelData.setRaining(true);
                    levelData.setThundering(false);
                }
                case STORM -> {
                    levelData.setClearWeatherTime(0);
                    levelData.setRainTime(duration);
                    levelData.setThunderTime(duration);
                    levelData.setRaining(true);
                    levelData.setThundering(true);
                }
            }
        }
    }

    private void spawnParticles() {
        switch (getWeatherType()) {
            case RAIN -> {
                spawnCloud(ParticleTypes.SMOKE, Math.PI / 5);
                for (int i = 0; i < 2; i++) {
                    double t = Math.PI * 2 * Math.random();
                    double a = 0.25 / (1.5 + Math.random());
                    double b = 0.35 / (1.5 + Math.random());
                    this.level().addParticle(ParticleTypes.FALLING_WATER,
                        this.getX() + a * Math.cos(t), this.getY() - 0.2, this.getZ() + b * Math.sin(t), 0, -0.05, 0);
                }
            }
            case STORM -> {
                spawnCloud(ParticleTypes.SMOKE, Math.PI / 5);
                double t = Math.PI * 2 * Math.random();
                double a = 0.25 / (1.5 + Math.random());
                double b = 0.35 / (1.5 + Math.random());
                this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + a * Math.cos(t), this.getY(), this.getZ() + b * Math.sin(t),
                    Math.random() * 0.1 - 0.05, Math.random() * 0.2 - 0.1, Math.random() * 0.1 - 0.05);
            }
            case SUN -> spawnCloud(ParticleTypes.CLOUD, Math.PI / 3);
        }
    }

    private void spawnCloud(net.minecraft.core.particles.SimpleParticleType particle, double step) {
        for (double y = -1; y <= 1; y += 1) {
            for (double t = 0; t < Math.PI * 2; t += step) {
                double a = 0.25 / (Math.abs(y) * 0.5 + 1);
                double b = 0.35 / (Math.abs(y) * 0.5 + 1);
                this.level().addParticle(particle,
                    this.getX() + a * Math.cos(t), this.getY() + y / 8, this.getZ() + b * Math.sin(t), 0, -0.03, 0);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(EGG_TYPE, compound.getInt("EggType"));
        this.entityData.set(AGE, compound.getInt("Age"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("EggType", this.entityData.get(EGG_TYPE));
        compound.putInt("Age", this.entityData.get(AGE));
    }
}
