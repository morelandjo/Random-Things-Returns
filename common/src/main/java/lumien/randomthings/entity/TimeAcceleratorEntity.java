package lumien.randomthings.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

/** Re-ticks the block (and its block entity) it sits on at a multiplied rate for its remaining duration. */
public class TimeAcceleratorEntity extends Entity {
    private static final EntityDataAccessor<Integer> TIME_RATE = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_TIME = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET_POS = SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.BLOCK_POS);

    public TimeAcceleratorEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setInvisible(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
    }

    public TimeAcceleratorEntity(Level level, BlockPos targetPos, int timeRate) {
        this(ModEntityTypes.TIME_ACCELERATOR.get(), level);
        setPos(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        this.entityData.set(TARGET_POS, targetPos);
        this.entityData.set(TIME_RATE, timeRate);
        this.entityData.set(REMAINING_TIME, 30 * 20);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TIME_RATE, 1);
        this.entityData.define(REMAINING_TIME, 30 * 20);
        this.entityData.define(TARGET_POS, BlockPos.ZERO);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            return;
        }
        int remainingTime = this.entityData.get(REMAINING_TIME);
        if (remainingTime <= 0) {
            this.discard();
            return;
        }
        this.entityData.set(REMAINING_TIME, remainingTime - 1);
        performAcceleration();
    }

    private void performAcceleration() {
        BlockPos targetPos = this.entityData.get(TARGET_POS);
        int timeRate = this.entityData.get(TIME_RATE);
        if (!level().isLoaded(targetPos)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) level();
        BlockState blockState = level().getBlockState(targetPos);
        if (blockState.isAir()) {
            this.discard();
            return;
        }

        BlockEntity blockEntity = level().getBlockEntity(targetPos);
        if (blockEntity != null) {
            boolean ticked = false;
            if (blockEntity instanceof TickingBlockEntity tickingBlockEntity) {
                for (int i = 0; i < timeRate; i++) {
                    try {
                        tickingBlockEntity.tick();
                        ticked = true;
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            if (!ticked && blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                for (int i = 0; i < timeRate; i++) {
                    try {
                        AbstractFurnaceBlockEntity.serverTick(serverLevel, targetPos, blockState, furnace);
                        ticked = true;
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            if (!ticked) {
                tickViaReflection(blockEntity, serverLevel, targetPos, blockState, timeRate);
            }
        }

        for (int i = 0; i < timeRate; i++) {
            if (blockState.isRandomlyTicking() && this.level().getRandom().nextInt(1365) == 0) {
                try {
                    blockState.randomTick(serverLevel, targetPos, serverLevel.random);
                } catch (Exception e) {
                    break;
                }
            }
        }
    }

    private void tickViaReflection(BlockEntity blockEntity, ServerLevel serverLevel, BlockPos targetPos, BlockState blockState, int timeRate) {
        try {
            Method tickMethod = null;
            Class<?> clazz = blockEntity.getClass();
            try {
                tickMethod = clazz.getMethod("serverTick", Level.class, BlockPos.class, BlockState.class, BlockEntity.class);
            } catch (NoSuchMethodException e1) {
                try {
                    tickMethod = clazz.getMethod("tick");
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (tickMethod != null) {
                for (int i = 0; i < timeRate; i++) {
                    try {
                        if (tickMethod.getParameterCount() == 4) {
                            tickMethod.invoke(null, serverLevel, targetPos, blockState, blockEntity);
                        } else {
                            tickMethod.invoke(blockEntity);
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public int getTimeRate() {
        return this.entityData.get(TIME_RATE);
    }

    public void setTimeRate(int timeRate) {
        this.entityData.set(TIME_RATE, timeRate);
    }

    public int getRemainingTime() {
        return this.entityData.get(REMAINING_TIME);
    }

    public void setRemainingTime(int remainingTime) {
        this.entityData.set(REMAINING_TIME, remainingTime);
    }

    public BlockPos getTargetPos() {
        return this.entityData.get(TARGET_POS);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.entityData.set(TIME_RATE, compound.getInt("timeRate"));
        this.entityData.set(REMAINING_TIME, compound.getInt("remainingTime"));
        this.entityData.set(TARGET_POS, new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("timeRate", this.entityData.get(TIME_RATE));
        compound.putInt("remainingTime", this.entityData.get(REMAINING_TIME));
        BlockPos targetPos = this.entityData.get(TARGET_POS);
        compound.putInt("targetX", targetPos.getX());
        compound.putInt("targetY", targetPos.getY());
        compound.putInt("targetZ", targetPos.getZ());
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 64.0 * 64.0;
    }
}
