package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpiritEntity extends FlyingMob {
    private static final EntityDataAccessor<Integer> SPIRIT_AGE = SynchedEntityData.defineId(SpiritEntity.class, EntityDataSerializers.INT);
    
    // Configuration values - matching the old mod
    public static final int SPIRIT_LIFETIME = 20 * 20; // 20 seconds in ticks
    
    private BlockPos spawnPosition;
    private int changePositionCounter = 50; // Start at 50 so first movement happens after 10 ticks (0.5 seconds)
    
    public SpiritEntity(EntityType<? extends SpiritEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SpiritMoveControl(this);
        this.setNoGravity(true);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPIRIT_AGE, 0);
    }
    
    @Override
    protected void registerGoals() {
        // No goals needed - we handle movement manually in tick()
    }
    
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingnavigation = new FlyingPathNavigation(this, level) {
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos).isSolid();
            }
        };
        flyingnavigation.setCanOpenDoors(false);
        flyingnavigation.setCanFloat(false);
        flyingnavigation.setCanPassDoors(true);
        return flyingnavigation;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (this.spawnPosition == null) {
            this.spawnPosition = this.blockPosition();
        }
        
        // Age the spirit
        int age = this.entityData.get(SPIRIT_AGE);
        this.entityData.set(SPIRIT_AGE, age + 1);
        
        // Die after lifetime expires
        if (!this.level().isClientSide && age > SPIRIT_LIFETIME) {
            this.discard();
            return;
        }
        
        // Handle movement AI (server-side only)
        if (!this.level().isClientSide) {
            this.changePositionCounter++;
            
            // Move every 60 ticks (3 seconds)
            if (this.changePositionCounter >= 60) {
                this.changePositionCounter = 0;
                
                // Find a new position near spawn
                BlockPos newTarget = null;
                for (int attempts = 0; attempts < 10; attempts++) {
                    int deltaX = this.random.nextInt(5) - 2;
                    int deltaY = this.random.nextInt(3);
                    int deltaZ = this.random.nextInt(5) - 2;
                    
                    BlockPos targetPos = this.spawnPosition.offset(deltaX, deltaY, deltaZ);
                    
                    if (this.level().getBlockState(targetPos).isAir()) {
                        newTarget = targetPos;
                        break;
                    }
                }
                
                if (newTarget != null) {
                    this.getMoveControl().setWantedPosition(
                        newTarget.getX() + 0.5, 
                        newTarget.getY() + 0.5, 
                        newTarget.getZ() + 0.5, 
                        0.02
                    );
                }
            }
        }
        
        // Client-side particles
        if (this.level().isClientSide && (this.xOld != this.getX() || this.yOld != this.getY() || this.zOld != this.getZ())) {
            if (this.random.nextDouble() < 0.5) {
                this.level().addParticle(ParticleTypes.ENCHANT,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.02,
                    this.random.nextDouble() * 0.02,
                    (this.random.nextDouble() - 0.5) * 0.02);
            }
        }
    }
    
    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        // Only take damage from magic sources, creative players, or void damage
        if (!damageSource.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO) && 
            !damageSource.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY) &&
            !(damageSource.getEntity() instanceof Player player && player.isCreative())) {
            return false;
        }
        
        return super.hurt(damageSource, amount);
    }
    
    @Override
    protected void dropAllDeathLoot(ServerLevel serverLevel, DamageSource damageSource) {
        super.dropAllDeathLoot(serverLevel, damageSource);
        
        // Drop ectoplasm when killed properly (not from timeout)
        if (damageSource != null && this.entityData.get(SPIRIT_AGE) < SPIRIT_LIFETIME) {
            int dropCount = 1;
            if (this.random.nextInt(5) == 0) {
                dropCount = 2;
            }
            
            for (int i = 0; i < dropCount; i++) {
                this.spawnAtLocation(new ItemStack(ModItems.ECTOPLASM.get()));
            }
        }
    }
    
    @Override
    public boolean isPushable() {
        return false;
    }
    
    @Override
    protected void pushEntities() {
        // Don't push other entities
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    
    @Override
    public void knockback(double strength, double x, double z) {
        // Immune to knockback
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SpiritAge", this.entityData.get(SPIRIT_AGE));
        if (this.spawnPosition != null) {
            compound.putInt("SpawnX", this.spawnPosition.getX());
            compound.putInt("SpawnY", this.spawnPosition.getY());
            compound.putInt("SpawnZ", this.spawnPosition.getZ());
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(SPIRIT_AGE, compound.getInt("SpiritAge"));
        if (compound.contains("SpawnX")) {
            this.spawnPosition = new BlockPos(
                compound.getInt("SpawnX"),
                compound.getInt("SpawnY"),
                compound.getInt("SpawnZ"));
        }
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return FlyingMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 1.0)
            .add(Attributes.FLYING_SPEED, 0.6)
            .add(Attributes.MOVEMENT_SPEED, 0.02);
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GHAST_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }
    
    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }
    
    // Custom move control for spirit movement
    static class SpiritMoveControl extends MoveControl {
        private final SpiritEntity spirit;
        
        public SpiritMoveControl(SpiritEntity spirit) {
            super(spirit);
            this.spirit = spirit;
        }
        
        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                double deltaX = this.wantedX - this.spirit.getX();
                double deltaY = this.wantedY - this.spirit.getY();
                double deltaZ = this.wantedZ - this.spirit.getZ();
                double distanceSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
                double distance = Math.sqrt(distanceSquared);
                
                if (this.canReach(this.wantedX, this.wantedY, this.wantedZ, distance) && distance > 0.2) {
                    // Apply movement directly to the entity's velocity
                    double speed = 0.01D;
                    this.spirit.setDeltaMovement(
                        this.spirit.getDeltaMovement().add(
                            (deltaX / distance) * speed,
                            (deltaY / distance) * speed,
                            (deltaZ / distance) * speed
                        )
                    );
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }
        
        private boolean canReach(double x, double y, double z, double distance) {
            double deltaX = (x - this.spirit.getX()) / distance;
            double deltaY = (y - this.spirit.getY()) / distance;
            double deltaZ = (z - this.spirit.getZ()) / distance;
            AABB boundingBox = this.spirit.getBoundingBox();
            
            for (int i = 1; i < distance; ++i) {
                boundingBox = boundingBox.move(deltaX, deltaY, deltaZ);
                if (!this.spirit.level().getBlockCollisions(this.spirit, boundingBox).iterator().hasNext()) {
                    continue;
                }
                return false;
            }
            
            return true;
        }
    }
}