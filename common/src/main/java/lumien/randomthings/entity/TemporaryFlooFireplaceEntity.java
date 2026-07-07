package lumien.randomthings.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Spawned when a Floo Token sits on the ground long enough. Acts as a temporary fireplace — standing
 * inside its bounding box and typing in chat teleports like a named Floo Brick. Lifetime ~13s.
 */
public class TemporaryFlooFireplaceEntity extends Entity {
    public static final int MAX_LIFETIME = 260;
    private static final EntityDataAccessor<Integer> AGE = SynchedEntityData.defineId(TemporaryFlooFireplaceEntity.class, EntityDataSerializers.INT);

    public TemporaryFlooFireplaceEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public TemporaryFlooFireplaceEntity(Level level, double x, double y, double z) {
        super(ModEntityTypes.TEMPORARY_FLOO_FIREPLACE.get(), level);
        this.noPhysics = true;
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(AGE, 0);
    }

    public int getAge() {
        return this.entityData.get(AGE);
    }

    @Override
    public void tick() {
        super.tick();
        int age = getAge();
        if (this.level().isClientSide) {
            if (age >= 7) {
                spawnFlameGrid();
            }
        } else if (age >= MAX_LIFETIME) {
            this.discard();
            return;
        }
        this.entityData.set(AGE, age + 1);
    }

    private void spawnFlameGrid() {
        for (double modX = -1; modX <= 1; modX += 0.2) {
            for (double modZ = -1; modZ <= 1; modZ += 0.2) {
                double offsetX = (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
                double offsetZ = (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
                this.level().addParticle(ParticleTypes.FLAME,
                    this.getX() + modX + offsetX,
                    this.getY() + 0.05,
                    this.getZ() + modZ + offsetZ,
                    0, 0, 0);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(AGE, compound.getInt("Age"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", getAge());
    }
}
