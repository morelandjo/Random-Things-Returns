package lumien.randomthings.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** A temporary portal formed by an Evil Tear on a valid frame; standing on it once charged goes to the End. */
public class ArtificialEndPortalEntity extends Entity {
    public int actionTimer;

    public ArtificialEndPortalEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public ArtificialEndPortalEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.ARTIFICIAL_END_PORTAL.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (actionTimer < 200) {
            actionTimer++;
            if (level().isClientSide && actionTimer > 40) {
                spawnParticles();
            }
        }
        if (!level().isClientSide) {
            if (tickCount % 40 == 0) {
                BlockPos center = new BlockPos((int) Math.floor(getX()), (int) Math.floor(getY()), (int) Math.floor(getZ()));
                if (!isValidPosition(level(), center, false)) {
                    this.discard();
                }
            }
        } else if (actionTimer == 85) {
            level().playLocalSound(getX(), getY(), getZ(), SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.2F, 1.0F, false);
        }
    }

    private void spawnParticles() {
        for (int i = 0; i < 5; i++) {
            double modX = Math.random() * 0.05 - 0.025;
            double modZ = Math.random() * 0.05 - 0.025;
            level().addParticle(ParticleTypes.ENCHANT, getX() + modX, getY() + 2.0, getZ() + modZ, modX * 2, 1.0, modZ * 2);
        }
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);
        if (!level().isClientSide && actionTimer >= 200
            && getBoundingBox().intersects(player.getBoundingBox()) && !player.isPassenger() && !player.isVehicle()
            && level() instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ServerLevel endLevel = serverLevel.getServer().getLevel(Level.END);
            if (endLevel != null) {
                BlockPos endSpawn = endLevel.getSharedSpawnPos();
                serverPlayer.teleportTo(endLevel, endSpawn.getX() + 0.5, endSpawn.getY(), endSpawn.getZ() + 0.5,
                    serverPlayer.getYRot(), serverPlayer.getXRot());
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.actionTimer = compound.getInt("actionTimer");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("actionTimer", actionTimer);
    }

    public static boolean isValidPosition(Level level, BlockPos center, boolean checkForOtherPortals) {
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!level.isEmptyBlock(center.offset(modX, 0, modZ))) {
                    return false;
                }
            }
        }
        for (int modY = 1; modY < 3; modY++) {
            if (!level.isEmptyBlock(center.above(modY))) {
                return false;
            }
        }
        if (!level.getBlockState(center.above(3)).is(Blocks.END_ROD) || !level.getBlockState(center.above(4)).is(Blocks.END_STONE)) {
            return false;
        }
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!level.getBlockState(center.offset(modX, -1, modZ)).is(Blocks.END_STONE)) {
                    return false;
                }
            }
        }
        for (int modX = -2; modX < 3; modX++) {
            for (int modZ = -2; modZ < 3; modZ++) {
                if ((modX == -2 || modZ == -2 || modX == 2 || modZ == 2)
                    && !level.getBlockState(center.offset(modX, 0, modZ)).is(Blocks.OBSIDIAN)) {
                    return false;
                }
            }
        }
        if (checkForOtherPortals) {
            AABB searchBox = new AABB(center.getX(), center.getY(), center.getZ(), center.getX() + 1, center.getY() + 2, center.getZ() + 1);
            List<ArtificialEndPortalEntity> portalList = level.getEntitiesOfClass(ArtificialEndPortalEntity.class, searchBox);
            if (!portalList.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
