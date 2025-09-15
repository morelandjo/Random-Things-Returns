package lumien.randomthings.entity;

import lumien.randomthings.item.ModDataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class StableEnderPearlEntity extends ItemEntity {
    private static final EntityDataAccessor<Integer> TELEPORT_TIMER = SynchedEntityData.defineId(StableEnderPearlEntity.class, EntityDataSerializers.INT);
    private static final int TELEPORT_DELAY = 140; // 7 seconds at 20 ticks per second

    public StableEnderPearlEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
    }

    public StableEnderPearlEntity(Level level, double x, double y, double z, ItemStack itemStack) {
        super(ModEntityTypes.STABLE_ENDER_PEARL.get(), level);
        this.setPos(x, y, z);
        this.setItem(itemStack);
        this.setPickUpDelay(TELEPORT_DELAY + 20); // Prevent pickup until after teleportation
        this.entityData.set(TELEPORT_TIMER, TELEPORT_DELAY);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TELEPORT_TIMER, TELEPORT_DELAY);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.isAlive()) {
            int timer = this.entityData.get(TELEPORT_TIMER);
            timer--;
            this.entityData.set(TELEPORT_TIMER, timer);

            // Spawn particles as countdown approaches
            if (timer < 60 && timer % 10 == 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        5, 0.5, 0.5, 0.5, 0.1);
                }
            }

            if (timer <= 0) {
                performTeleport();
            }
        }
    }

    private void performTeleport() {
        ItemStack itemStack = this.getItem();
        UUID boundPlayerUUID = itemStack.get(ModDataComponents.PLAYER_UUID.get());

        if (boundPlayerUUID != null) {
            // Try to teleport the bound player
            if (this.level() instanceof ServerLevel serverLevel) {
                ServerPlayer boundPlayer = serverLevel.getServer().getPlayerList().getPlayer(boundPlayerUUID);
                if (boundPlayer != null) {
                    teleportEntity(boundPlayer);
                    this.discard();
                    return;
                }
            }
        }

        // If no bound player or player not found, teleport random entity
        teleportRandomEntity();
        this.discard();
    }

    private void teleportEntity(Entity entity) {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Play teleport sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);

            // Spawn particles at source location
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);

            // Teleport entity to pearl location
            entity.teleportTo(this.getX(), this.getY(), this.getZ());

            // Spawn particles at destination
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                this.getX(), this.getY() + 0.5, this.getZ(),
                20, 0.5, 0.5, 0.5, 0.1);

            // Play teleport sound at destination
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
        }
    }

    private void teleportRandomEntity() {
        AABB searchArea = new AABB(
            this.getX() - 10, this.getY() - 10, this.getZ() - 10,
            this.getX() + 10, this.getY() + 10, this.getZ() + 10
        );

        List<Entity> nearbyEntities = this.level().getEntitiesOfClass(Entity.class, searchArea,
            entity -> entity != this && entity.isAlive() && !(entity instanceof ItemEntity));

        if (!nearbyEntities.isEmpty()) {
            Entity randomEntity = nearbyEntities.get(this.level().random.nextInt(nearbyEntities.size()));
            teleportEntity(randomEntity);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("TeleportTimer", this.entityData.get(TELEPORT_TIMER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(TELEPORT_TIMER, compound.getInt("TeleportTimer"));
    }
}