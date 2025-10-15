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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

import java.util.Random;

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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(EGG_TYPE, 0);
        builder.define(AGE, 0);
    }

    public WeatherEggItem.WeatherType getWeatherType() {
        int typeOrdinal = this.entityData.get(EGG_TYPE);
        WeatherEggItem.WeatherType[] types = WeatherEggItem.WeatherType.values();
        if (typeOrdinal >= 0 && typeOrdinal < types.length) {
            return types[typeOrdinal];
        }
        return WeatherEggItem.WeatherType.SUN;
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    @Override
    public void tick() {
        super.tick();

        int age = this.entityData.get(AGE);

        // Phase 1: Slow rise (first 200 ticks)
        if (age < 200) {
            this.setDeltaMovement(0, 0.007, 0);
        }
        // Phase 2: Accelerating rise until reaching sky
        else {
            if (this.getY() < this.level().getHeight()) {
                double motionY = this.getDeltaMovement().y;
                motionY += 0.001;
                motionY *= 1.02;
                this.setDeltaMovement(0, motionY, 0);
            }
            // Phase 3: Reached sky - trigger weather change
            else if (!this.level().isClientSide) {
                changeWeather();
                this.discard();
                return;
            }
        }

        // Apply motion
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        // Spawn particles on client side
        if (this.level().isClientSide) {
            spawnParticles();
        }

        // Increment age
        this.entityData.set(AGE, age + 1);
    }

    private void changeWeather() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ServerLevelData levelData = (ServerLevelData) serverLevel.getLevelData();
            Random random = new Random();
            int duration = (300 + random.nextInt(600)) * 20; // 5-15 minutes

            WeatherEggItem.WeatherType type = getWeatherType();
            switch (type) {
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
        WeatherEggItem.WeatherType type = getWeatherType();

        switch (type) {
            case RAIN -> {
                spawnDefaultCloud();
                // Rain drops falling from cloud
                for (int i = 0; i < 2; i++) {
                    double t = Math.PI * 2 * Math.random();
                    double a = 0.25;
                    double b = 0.35;
                    a /= 1.5 + Math.random();
                    b /= 1.5 + Math.random();
                    double elX = a * Math.cos(t);
                    double elZ = b * Math.sin(t);
                    this.level().addParticle(ParticleTypes.FALLING_WATER,
                        this.getX() + elX, this.getY() - 0.2, this.getZ() + elZ,
                        0, -0.05, 0);
                }
            }
            case STORM -> {
                spawnDefaultCloud();
                // Lightning sparks
                double t = Math.PI * 2 * Math.random();
                double a = 0.25;
                double b = 0.35;
                a /= 1.5 + Math.random();
                b /= 1.5 + Math.random();
                double elX = a * Math.cos(t);
                double elZ = b * Math.sin(t);
                // Use yellow/gold colored effect - using end rod for lightning effect
                this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX() + elX, this.getY(), this.getZ() + elZ,
                    Math.random() * 0.1 - 0.05, Math.random() * 0.2 - 0.1, Math.random() * 0.1 - 0.05);
            }
            case SUN -> {
                spawnNiceCloud();
            }
        }
    }

    private void spawnDefaultCloud() {
        // Create gray smoke cloud in elliptical pattern
        for (double y = -1; y <= 1; y += 1) {
            for (double t = 0; t < Math.PI * 2; t += Math.PI / 5) {
                double a = 0.25;
                double b = 0.35;
                a /= Math.abs(y) * 0.5 + 1;
                b /= Math.abs(y) * 0.5 + 1;
                double elX = a * Math.cos(t);
                double elZ = b * Math.sin(t);
                this.level().addParticle(ParticleTypes.SMOKE,
                    this.getX() + elX, this.getY() + y / 8, this.getZ() + elZ,
                    0, -0.03, 0);
            }
        }
    }

    private void spawnNiceCloud() {
        // Create white/bright cloud for sunny weather
        for (double y = -1; y <= 1; y += 1) {
            for (double t = 0; t < Math.PI * 2; t += Math.PI / 3) {
                double a = 0.25;
                double b = 0.35;
                a /= Math.abs(y) * 0.5 + 1;
                b /= Math.abs(y) * 0.5 + 1;
                double elX = a * Math.cos(t);
                double elZ = b * Math.sin(t);
                // Use cloud particles for nice white effect
                this.level().addParticle(ParticleTypes.CLOUD,
                    this.getX() + elX, this.getY() + y / 8, this.getZ() + elZ,
                    0, -0.03, 0);
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
