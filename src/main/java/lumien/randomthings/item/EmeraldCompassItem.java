package lumien.randomthings.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class EmeraldCompassItem extends Item {
    
    @OnlyIn(Dist.CLIENT)
    private double rotation;
    @OnlyIn(Dist.CLIENT)
    private double rota;
    @OnlyIn(Dist.CLIENT)
    private long lastUpdateTick;

    public EmeraldCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide && level.getGameTime() % 20 == 0) {
            updateCompassTarget(stack, level);
        }
    }

    private void updateCompassTarget(ItemStack stack, Level level) {
        UUID playerUUID = stack.get(ModDataComponents.PLAYER_UUID.get());
        
        if (playerUUID != null && ServerLifecycleHooks.getCurrentServer() != null) {
            ServerPlayer targetPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
            
            if (targetPlayer != null) {
                BlockPos targetPos = targetPlayer.blockPosition();
                stack.set(ModDataComponents.COMPASS_TARGET_X.get(), targetPos.getX());
                stack.set(ModDataComponents.COMPASS_TARGET_Z.get(), targetPos.getZ());
            } else {
                // Remove target position if player is not found
                stack.remove(ModDataComponents.COMPASS_TARGET_X.get());
                stack.remove(ModDataComponents.COMPASS_TARGET_Z.get());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getCompassAngle(ItemStack stack, @Nullable Level level, @Nullable LivingEntity entity) {
        if (entity == null && !stack.isFramed()) {
            return 0.0F;
        } else {
            boolean flag = entity != null;
            Entity compassEntity = flag ? entity : stack.getFrame();

            if (level == null) {
                level = compassEntity.level();
            }

            double angle;
            boolean hasTarget = hasTarget(stack);

            if (hasTarget && level instanceof ClientLevel) {
                Integer targetX = stack.get(ModDataComponents.COMPASS_TARGET_X.get());
                Integer targetZ = stack.get(ModDataComponents.COMPASS_TARGET_Z.get());
                
                if (targetX != null && targetZ != null) {
                    double entityRotation = flag ? (double)compassEntity.getYRot() : this.getFrameRotation((ItemFrame)compassEntity);
                    entityRotation = entityRotation % 360.0D;
                    double angleToPos = this.getAngleToPos((ClientLevel)level, compassEntity, new BlockPos(targetX, 0, targetZ));
                    angle = Math.PI - ((entityRotation - 90.0D) * 0.01745329238474369D - angleToPos);
                } else {
                    angle = Math.random() * (Math.PI * 2D);
                }
            } else {
                angle = Math.random() * (Math.PI * 2D);
            }

            if (flag && !hasTarget) {
                angle = this.wobble((ClientLevel)level, angle);
            }

            float result = (float)(angle / (Math.PI * 2D));
            return Mth.positiveModulo(result, 1.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private double wobble(ClientLevel level, double baseAngle) {
        if (level.getGameTime() != this.lastUpdateTick) {
            this.lastUpdateTick = level.getGameTime();
            double d0 = baseAngle - this.rotation;
            d0 = d0 % (Math.PI * 2D);
            d0 = Mth.clamp(d0, -1.0D, 1.0D);
            this.rota += d0 * 0.1D;
            this.rota *= 0.8D;
            this.rotation += this.rota;
        }

        return this.rotation;
    }

    @OnlyIn(Dist.CLIENT)
    private double getFrameRotation(ItemFrame itemFrame) {
        return Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
    }

    @OnlyIn(Dist.CLIENT)
    private double getAngleToPos(ClientLevel level, Entity entity, BlockPos pos) {
        return Math.atan2(pos.getZ() - entity.getZ(), pos.getX() - entity.getX());
    }

    private boolean hasTarget(ItemStack stack) {
        return stack.has(ModDataComponents.COMPASS_TARGET_X.get()) && stack.has(ModDataComponents.COMPASS_TARGET_Z.get());
    }

    public static void setTarget(ItemStack compass, UUID playerUUID) {
        compass.set(ModDataComponents.PLAYER_UUID.get(), playerUUID);
    }

    public static UUID getTargetUUID(ItemStack compass) {
        return compass.get(ModDataComponents.PLAYER_UUID.get());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }
}