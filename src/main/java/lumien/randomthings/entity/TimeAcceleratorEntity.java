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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import java.lang.reflect.Method;
import net.minecraft.world.level.block.state.BlockState;

public class TimeAcceleratorEntity extends Entity {
    
    private static final EntityDataAccessor<Integer> TIME_RATE = 
        SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_TIME = 
        SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockPos> TARGET_POS = 
        SynchedEntityData.defineId(TimeAcceleratorEntity.class, EntityDataSerializers.BLOCK_POS);
        
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
        this.entityData.set(REMAINING_TIME, 30 * 20); // 30 seconds
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TIME_RATE, 1);
        builder.define(REMAINING_TIME, 30 * 20);
        builder.define(TARGET_POS, BlockPos.ZERO);
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
        
        // Perform acceleration
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
        
        // Accelerate block entity ticks like original implementation
        BlockEntity blockEntity = level().getBlockEntity(targetPos);
        
        if (blockEntity != null) {
            // Try different ways to tick the block entity based on its type
            boolean ticked = false;
            
            // Method 1: Check if it's a TickingBlockEntity (rare in modern versions)
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
            
            // Method 2: Handle specific block entity types manually
            if (!ticked && blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
                for (int i = 0; i < timeRate; i++) {
                    try {
                        // Call the static serverTick method for furnaces
                        AbstractFurnaceBlockEntity.serverTick(serverLevel, targetPos, blockState, furnace);
                        ticked = true;
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            
            // Method 3: Use reflection to find and call a tick method (fallback)
            if (!ticked) {
                try {
                    // Look for common tick method names
                    Method tickMethod = null;
                    Class<?> clazz = blockEntity.getClass();
                    
                    try {
                        // Try serverTick first (most common in modern versions)
                        tickMethod = clazz.getMethod("serverTick", Level.class, BlockPos.class, BlockState.class, BlockEntity.class);
                    } catch (NoSuchMethodException e1) {
                        try {
                            // Try tick method
                            tickMethod = clazz.getMethod("tick");
                        } catch (NoSuchMethodException e2) {
                            // No tick method found
                        }
                    }
                    
                    if (tickMethod != null) {
                        for (int i = 0; i < timeRate; i++) {
                            try {
                                if (tickMethod.getParameterCount() == 4) {
                                    // Static serverTick method
                                    tickMethod.invoke(null, serverLevel, targetPos, blockState, blockEntity);
                                } else {
                                    // Instance tick method
                                    tickMethod.invoke(blockEntity);
                                }
                                ticked = true;
                            } catch (Exception e) {
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    // Reflection failed silently
                }
            }
        }
        
        // Random block ticks with proper probability like original (1/1365 chance per acceleration)
        for (int i = 0; i < timeRate; i++) {
            if (blockState.isRandomlyTicking() && this.level().getRandom().nextInt(1365) == 0) {
                try {
                    blockState.randomTick(serverLevel, targetPos, serverLevel.random);
                } catch (Exception e) {
                    // Prevent crashes from broken random tick logic
                    break;
                }
            }
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
        this.entityData.set(TARGET_POS, new BlockPos(
            compound.getInt("targetX"),
            compound.getInt("targetY"), 
            compound.getInt("targetZ")
        ));
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
        // Render up to 64 blocks away
        return distance < 64.0 * 64.0;
    }
}