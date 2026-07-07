package lumien.randomthings.entity;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.TimeInABottleItem;
import lumien.randomthings.network.EclipsedClockAnimationPacket;
import lumien.randomthings.network.RTNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
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

/**
 * Wall-hung clock with a configurable target time (right-click ±10 minutes, shift to reverse).
 * Use a Time in a Bottle on it to spend stored time and fast-forward the world to the target.
 */
public class EclipsedClockEntity extends Entity {
    private static final EntityDataAccessor<Integer> TARGET_TIME = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FACING_DIRECTION = SynchedEntityData.defineId(EclipsedClockEntity.class, EntityDataSerializers.INT);

    private BlockPos attachmentPos;
    public int timeDisplayCounter;
    public int animationCounter;
    private int cooldownCounter;

    public EclipsedClockEntity(EntityType<? extends Entity> entityType, Level level) {
        super(entityType, level);
    }

    public EclipsedClockEntity(Level level, BlockPos pos, Direction direction) {
        super(ModEntityTypes.ECLIPSED_CLOCK.get(), level);
        this.attachmentPos = pos.immutable();
        double offset = 0.03;
        this.setPos(
            pos.getX() + 0.5 + direction.getStepX() * (0.5 + offset),
            pos.getY() + 0.5 + direction.getStepY() * (0.5 + offset),
            pos.getZ() + 0.5 + direction.getStepZ() * (0.5 + offset));
        this.entityData.set(FACING_DIRECTION, direction.get3DDataValue());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_TIME, 0);
        this.entityData.define(FACING_DIRECTION, Direction.SOUTH.get3DDataValue());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("targetTime", getTargetTime());
        compound.putByte("facing", (byte) this.getDirection().get3DDataValue());
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
            this.entityData.set(FACING_DIRECTION, (int) compound.getByte("facing"));
        }
        if (compound.contains("attachX")) {
            this.attachmentPos = new BlockPos(compound.getInt("attachX"), compound.getInt("attachY"), compound.getInt("attachZ"));
        }
    }

    @Override
    public Direction getDirection() {
        return Direction.from3DDataValue(this.entityData.get(FACING_DIRECTION));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount % 10 == 0 && !survives()) {
            this.dropItem(null);
            this.discard();
            return;
        }
        if (this.level().isClientSide) {
            if (this.timeDisplayCounter > 0) {
                this.timeDisplayCounter--;
            }
            if (this.animationCounter > 0) {
                this.animationCounter--;
            }
        } else if (cooldownCounter > 0) {
            cooldownCounter--;
        }
    }

    public void triggerAnimation() {
        if (this.animationCounter == 0) {
            this.animationCounter = 100;
        }
        this.timeDisplayCounter = 60;
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
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.getItem() instanceof TimeInABottleItem) {
            if (!this.level().isClientSide && cooldownCounter == 0) {
                int storedTime = TimeInABottleItem.getStoredTime(heldItem);
                int currentWorldTime = (int) this.level().getDayTime();
                int timeDifference = (getTargetTime() - currentWorldTime) % 24000;
                if (timeDifference < 0) {
                    timeDifference += 24000;
                }
                if (storedTime >= timeDifference || player.getAbilities().instabuild) {
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.setDayTime(serverLevel.getDayTime() + timeDifference);
                    }
                    if (!player.getAbilities().instabuild) {
                        TimeInABottleItem.setStoredTime(heldItem, storedTime - timeDifference);
                    }
                    cooldownCounter = 110;
                    sendAnimationToNearby();
                    this.level().playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.3F, 1.2F);
                    return InteractionResult.SUCCESS;
                }
                player.displayClientMessage(Component.translatable("item.randomthings.time_in_a_bottle.insufficient_time_for_clock")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
            return InteractionResult.SUCCESS;
        }

        if (!this.level().isClientSide) {
            if (cooldownCounter == 0) {
                int targetTime = getTargetTime() + (player.isShiftKeyDown() ? -20 * 30 : 20 * 30);
                targetTime = targetTime % 24000;
                if (targetTime < 0) {
                    targetTime += 24000;
                }
                setTargetTime(targetTime);
            }
            sendAnimationToNearby();
        }
        return InteractionResult.SUCCESS;
    }

    /** Notifies nearby clients to show the time display / spin animation. */
    private void sendAnimationToNearby() {
        if (this.level() instanceof ServerLevel serverLevel) {
            EclipsedClockAnimationPacket packet = new EclipsedClockAnimationPacket(this.getId());
            for (ServerPlayer viewer : serverLevel.players()) {
                if (viewer.distanceToSqr(this) < 64 * 64) {
                    RTNetwork.sendToPlayer(viewer, packet);
                }
            }
        }
    }

    public int getTargetTime() {
        return this.entityData.get(TARGET_TIME);
    }

    public void setTargetTime(int newTime) {
        this.entityData.set(TARGET_TIME, newTime);
    }

    public String getStringTargetTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        try {
            Date d = df.parse("06:00");
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            cal.add(Calendar.MINUTE, (int) (1440 / 24000D * getTargetTime()));
            return df.format(cal.getTime());
        } catch (ParseException e) {
            return "XX:XX";
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.375F, 0.375F);
    }

    @Override
    protected AABB makeBoundingBox() {
        double halfFace = 6.0 / 32.0;
        double halfThick = 1.0 / 32.0;
        Direction direction = this.getDirection();
        if (direction.getAxis() == Direction.Axis.X) {
            return new AABB(getX() - halfThick, getY() - halfFace, getZ() - halfFace,
                getX() + halfThick, getY() + halfFace, getZ() + halfFace);
        } else if (direction.getAxis() == Direction.Axis.Z) {
            return new AABB(getX() - halfFace, getY() - halfFace, getZ() - halfThick,
                getX() + halfFace, getY() + halfFace, getZ() + halfThick);
        }
        return new AABB(getX() - halfFace, getY() - halfThick, getZ() - halfFace,
            getX() + halfFace, getY() + halfThick, getZ() + halfFace);
    }

    public boolean survives() {
        if (!this.level().isClientSide && this.attachmentPos != null) {
            return this.level().getBlockState(this.attachmentPos).isSolid();
        }
        return true;
    }

    public void dropItem(@javax.annotation.Nullable Entity breaker) {
        if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (breaker instanceof Player player && player.getAbilities().instabuild) {
                return;
            }
            this.spawnAtLocation(new ItemStack(ModItems.ECLIPSED_CLOCK.get()), 0.0F);
        }
    }

    public void playPlacementSound() {
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        if (entity instanceof Player player) {
            if (!this.level().isClientSide) {
                this.dropItem(player);
                this.discard();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
}
