package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.TimeInABottleItem;
import lumien.randomthings.network.EclipsedClockAnimationPacket;
import lumien.randomthings.network.RTPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Predicate;

public class EclipsedClockEntity extends Entity {
    private static final EntityDataAccessor<Integer> TARGET_TIME = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FACING_DIRECTION = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.INT);
    
    private BlockPos attachmentPos;
    private Direction facingDirection;
    
    public int timeDisplayCounter;
    public int animationCounter;
    private int cooldownCounter;
    
    public EclipsedClockEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }
    
    public EclipsedClockEntity(Level level, BlockPos pos, Direction direction) {
        super(ModEntityTypes.ECLIPSED_CLOCK.get(), level);
        
        // Store attachment position and direction
        this.attachmentPos = pos.immutable();
        this.facingDirection = direction;
        
        // Position entity slightly outside the clicked face surface
        double offset = 0.03;
        double x = pos.getX() + 0.5 + direction.getStepX() * (0.5 + offset);
        double y = pos.getY() + 0.5 + direction.getStepY() * (0.5 + offset); 
        double z = pos.getZ() + 0.5 + direction.getStepZ() * (0.5 + offset);
        
        this.setPos(x, y, z);
        
        // Sync the direction to client
        this.entityData.set(FACING_DIRECTION, direction.get3DDataValue());
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TARGET_TIME, 0);
        builder.define(FACING_DIRECTION, Direction.SOUTH.get3DDataValue());
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("targetTime", getTargetTime());
        compound.putByte("facing", (byte)this.getDirection().get3DDataValue());
        if (this.attachmentPos != null) {
            compound.putInt("attachX", this.attachmentPos.getX());
            compound.putInt("attachY", this.attachmentPos.getY());
            compound.putInt("attachZ", this.attachmentPos.getZ());
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        setTargetTime(compound.getInt("targetTime"));
        
        if (compound.contains("facing")) {
            Direction loadedDirection = Direction.from3DDataValue(compound.getByte("facing"));
            this.facingDirection = loadedDirection;
            this.entityData.set(FACING_DIRECTION, loadedDirection.get3DDataValue());
        }
        
        if (compound.contains("attachX")) {
            this.attachmentPos = new BlockPos(
                compound.getInt("attachX"),
                compound.getInt("attachY"), 
                compound.getInt("attachZ")
            );
        }
    }
    
    public Direction getDirection() {
        return Direction.from3DDataValue(this.entityData.get(FACING_DIRECTION));
    }
    
    public void setDirection(Direction direction) {
        this.facingDirection = direction;
        this.entityData.set(FACING_DIRECTION, direction.get3DDataValue());
    }
    
    public BlockPos getAttachmentPos() {
        return this.attachmentPos;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Check survival every 10 ticks on server side
        if (!this.level().isClientSide && this.tickCount % 10 == 0) {
            if (!survives()) {
                this.dropItem(null);
                this.discard();
                return;
            }
        }
        
        if (this.level().isClientSide) {
            if (this.timeDisplayCounter > 0) {
                this.timeDisplayCounter--;
            }
            
            if (this.animationCounter > 0) {
                this.animationCounter--;
            }
        } else {
            if (cooldownCounter > 0) {
                cooldownCounter--;
            }
        }
    }
    
    public void triggerAnimation() {
        System.out.println("=== TRIGGER ANIMATION CALLED ===");
        System.out.println("Current animation counter: " + this.animationCounter);
        System.out.println("Current time display counter: " + this.timeDisplayCounter);
        
        if (this.animationCounter == 0) {
            this.animationCounter = 100;
        }
        // Also show the time display when animation is triggered
        this.timeDisplayCounter = 60;
        
        System.out.println("New animation counter: " + this.animationCounter);
        System.out.println("New time display counter: " + this.timeDisplayCounter);
    }
    
    public boolean shouldDisplayTime() {
        return this.timeDisplayCounter > 0;
    }
    
    @Override
    public InteractionResult interactAt(Player player, Vec3 vec, InteractionHand hand) {
        return this.interact(player, hand);
    }
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        System.out.println("=== CLOCK INTERACTION ===");
        System.out.println("Player: " + player.getName().getString());
        System.out.println("Hand: " + hand);
        System.out.println("Is client side: " + this.level().isClientSide);
        
        ItemStack heldItem = player.getItemInHand(hand);
        
        // Check if holding Time in a Bottle
        if (heldItem.getItem() instanceof TimeInABottleItem) {
            if (!this.level().isClientSide && cooldownCounter == 0) {
                int storedTime = TimeInABottleItem.getStoredTime(heldItem);
                int currentWorldTime = (int) this.level().getDayTime();
                int targetTime = getTargetTime();
                int timeDifference = (targetTime - currentWorldTime) % 24000;
                
                if (timeDifference < 0) {
                    timeDifference += 24000;
                }
                
                if (storedTime >= timeDifference || player.getAbilities().instabuild) {
                    // Set world time to target time
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.setDayTime(serverLevel.getDayTime() + timeDifference);
                    }
                    
                    if (!player.getAbilities().instabuild) {
                        TimeInABottleItem.setStoredTime(heldItem, storedTime - timeDifference);
                    }
                    
                    cooldownCounter = 110;
                    
                    // Trigger animation and sound
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) this.level(), 
                        this.level().getChunkAt(this.blockPosition()).getPos(), 
                        new EclipsedClockAnimationPacket(this.getId()));
                    this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, net.minecraft.sounds.SoundSource.BLOCKS, 0.3F, 1.2F);
                    
                    return InteractionResult.SUCCESS;
                } else {
                    // Not enough time stored
                    player.displayClientMessage(net.minecraft.network.chat.Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time_for_clock")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.SUCCESS;
        } else {
            // Normal interaction - adjust target time and show display
            System.out.println("=== NORMAL INTERACTION ===");
            System.out.println("Is client side: " + this.level().isClientSide);
            System.out.println("Cooldown counter: " + cooldownCounter);
            
            if (!this.level().isClientSide) {
                if (cooldownCounter == 0) {
                    int targetTime = getTargetTime();
                    System.out.println("Current target time: " + targetTime);
                    
                    if (player.isShiftKeyDown()) {
                        targetTime -= 20 * 30; // Decrease by 10 minutes (600 seconds = 20*30 ticks)
                        System.out.println("Shift clicked - decreasing time");
                    } else {
                        targetTime += 20 * 30; // Increase by 10 minutes
                        System.out.println("Normal click - increasing time");
                    }
                    
                    targetTime = targetTime % 24000;
                    if (targetTime < 0) {
                        targetTime += 24000;
                    }
                    
                    System.out.println("New target time: " + targetTime);
                    setTargetTime(targetTime);
                }
                
                // Always send packet to show time display on client when right-clicking
                if (this.level() instanceof ServerLevel serverLevel) {
                    System.out.println("Sending animation packet to show time display");
                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(this, 
                        new EclipsedClockAnimationPacket(this.getId()));
                }
            }
            
            return InteractionResult.SUCCESS;
        }
    }
    
    public int getTargetTime() {
        return this.entityData.get(TARGET_TIME);
    }
    
    public void setTargetTime(int newTime) {
        this.entityData.set(TARGET_TIME, newTime);
    }
    
    public String getStringTargetTime() {
        String myTime = "06:00";
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        try {
            Date d = df.parse(myTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, (int) (1440 / 24000D * getTargetTime()));
            return df.format(cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return "XX:XX";
    }
    
    
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.375F, 0.375F); // 12/32 = 0.375
    }
    
    @Override
    protected AABB makeBoundingBox() {
        double centerX = this.getX();
        double centerY = this.getY(); 
        double centerZ = this.getZ();
        
        Direction direction = this.getDirection();
        double halfFace = 6.0/32.0; // 6 pixels = half of 12
        double halfThick = 1.0/32.0; // 1 pixel = half of 2
        
        if (direction.getAxis() == Direction.Axis.X) {
            // East/West: thin in X, wide in Y/Z
            return new AABB(centerX - halfThick, centerY - halfFace, centerZ - halfFace,
                           centerX + halfThick, centerY + halfFace, centerZ + halfFace);
        } else if (direction.getAxis() == Direction.Axis.Z) {
            // North/South: thin in Z, wide in X/Y  
            return new AABB(centerX - halfFace, centerY - halfFace, centerZ - halfThick,
                           centerX + halfFace, centerY + halfFace, centerZ + halfThick);
        } else {
            // Up/Down: thin in Y, wide in X/Z
            return new AABB(centerX - halfFace, centerY - halfThick, centerZ - halfFace,
                           centerX + halfFace, centerY + halfThick, centerZ + halfFace);
        }
    }
    
    public boolean survives() {
        // Simple survives check - just verify attachment block is solid
        if (!this.level().isClientSide && this.attachmentPos != null) {
            return this.level().getBlockState(this.attachmentPos).isSolid();
        }
        return true; // Always survive on client side
    }
    
    public void dropItem(Entity breaker) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            
            if (breaker instanceof Player player && player.getAbilities().instabuild) {
                return;
            }
            
            this.spawnAtLocation(new ItemStack(ModItems.ECLIPSED_CLOCK.get()), 0.0F);
        }
    }
    
    public void playPlacementSound() {
        // No placement sound
    }
    
    
    @Override
    public Vec3 trackingPosition() {
        return this.position();
    }
    
    public int getAnimationCounter() {
        return animationCounter;
    }
    
    @Override
    public boolean isPickable() {
        return true; // Make entity interactable
    }
    
    @Override 
    public boolean skipAttackInteraction(Entity entity) {
        return entity instanceof Player; // Don't attack players, allow interaction
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return true; // Allow collision detection for interactions
    }
}