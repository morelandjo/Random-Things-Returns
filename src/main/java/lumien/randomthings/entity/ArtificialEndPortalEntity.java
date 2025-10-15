package lumien.randomthings.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ArtificialEndPortalEntity extends Entity {
    public int actionTimer;

    public ArtificialEndPortalEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setBoundingBox(new AABB(
            getX() - 1.5, getY(), getZ() - 1.5,
            getX() + 1.5, getY() + 1.0, getZ() + 1.5
        ));
    }

    public ArtificialEndPortalEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.ARTIFICIAL_END_PORTAL.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synced data needed
    }

    @Override
    public void tick() {
        super.tick();

        if (actionTimer < 200) {
            actionTimer++;

            // Spawn particles on client side after 40 ticks
            if (level().isClientSide && actionTimer > 40) {
                spawnParticles();
            }
        }

        // Server-side validation and sound
        if (!level().isClientSide) {
            // Check structure integrity every 40 ticks
            if (tickCount % 40 == 0) {
                BlockPos center = new BlockPos((int) Math.floor(getX()), (int) Math.floor(getY()), (int) Math.floor(getZ()));
                if (!isValidPosition(level(), center, false)) {
                    this.discard();
                }
            }
        } else {
            // Client-side sound at tick 85
            if (actionTimer == 85) {
                level().playLocalSound(getX(), getY(), getZ(),
                                     SoundEvents.END_PORTAL_SPAWN,
                                     SoundSource.BLOCKS, 0.2F, 1.0F, false);
            }
        }
    }

    private void spawnParticles() {
        // Spawn purple enchantment table particles
        for (int i = 0; i < 5; i++) {
            double modX = Math.random() * 0.05 - 0.025;
            double modZ = Math.random() * 0.05 - 0.025;

            double startX = getX() + modX;
            double startY = getY() + 2.0;
            double startZ = getZ() + modZ;

            level().addParticle(ParticleTypes.ENCHANT,
                              startX, startY, startZ,
                              modX * 2, 1.0, modZ * 2);
        }
    }

    @Override
    public void playerTouch(Player player) {
        super.playerTouch(player);

        // Teleport player to The End when portal is fully activated
        if (!level().isClientSide && actionTimer >= 200) {
            if (getBoundingBox().intersects(player.getBoundingBox()) &&
                !player.isPassenger() && !player.isVehicle()) {

                // Teleport to The End
                if (level() instanceof ServerLevel serverLevel) {
                    ServerLevel endLevel = serverLevel.getServer().getLevel(Level.END);
                    if (endLevel != null) {
                        // Get the End's spawn point
                        BlockPos endSpawn = endLevel.getSharedSpawnPos();
                        Vec3 spawnPos = new Vec3(endSpawn.getX() + 0.5, endSpawn.getY(), endSpawn.getZ() + 0.5);

                        // Create dimension transition
                        DimensionTransition transition = new DimensionTransition(
                            endLevel,
                            spawnPos,
                            player.getDeltaMovement(),
                            player.getYRot(),
                            player.getXRot(),
                            DimensionTransition.DO_NOTHING
                        );

                        player.changeDimension(transition);
                    }
                }
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

    /**
     * Validates if the portal structure is correct at the given position
     * @param level The level to check in
     * @param center The center position (3 blocks below the End Rod)
     * @param checkForOtherPortals Whether to check for existing portals
     * @return true if structure is valid
     */
    public static boolean isValidPosition(Level level, BlockPos center, boolean checkForOtherPortals) {
        // Check 3x3 area at Y level is air
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!level.isEmptyBlock(center.offset(modX, 0, modZ))) {
                    return false;
                }
            }
        }

        // Check 3 blocks above center is air
        for (int modY = 1; modY < 3; modY++) {
            if (!level.isEmptyBlock(center.above(modY))) {
                return false;
            }
        }

        // Check End Rod at Y+3 and End Stone at Y+4
        if (!level.getBlockState(center.above(3)).is(Blocks.END_ROD) ||
            !level.getBlockState(center.above(4)).is(Blocks.END_STONE)) {
            return false;
        }

        // Check 3x3 base of End Stone at Y-1
        for (int modX = -1; modX < 2; modX++) {
            for (int modZ = -1; modZ < 2; modZ++) {
                if (!level.getBlockState(center.offset(modX, -1, modZ)).is(Blocks.END_STONE)) {
                    return false;
                }
            }
        }

        // Check 5x5 ring of Obsidian at Y level (edges only)
        for (int modX = -2; modX < 3; modX++) {
            for (int modZ = -2; modZ < 3; modZ++) {
                if (modX == -2 || modZ == -2 || modX == 2 || modZ == 2) {
                    if (!level.getBlockState(center.offset(modX, 0, modZ)).is(Blocks.OBSIDIAN)) {
                        return false;
                    }
                }
            }
        }

        // Check for other portals if requested
        if (checkForOtherPortals) {
            BlockPos maxPos = center.offset(1, 2, 1);
            AABB searchBox = new AABB(
                center.getX(), center.getY(), center.getZ(),
                maxPos.getX(), maxPos.getY(), maxPos.getZ()
            );
            List<ArtificialEndPortalEntity> portalList = level.getEntitiesOfClass(
                ArtificialEndPortalEntity.class, searchBox);

            if (!portalList.isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
