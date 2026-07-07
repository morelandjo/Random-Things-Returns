package lumien.randomthings.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Client needle logic for the Emerald/Golden compasses: computes the {@code randomthings:angle}
 * model-predicate value toward a stack-stored target, with the vanilla compass wobble when unbound.
 */
@Environment(EnvType.CLIENT)
public final class CompassAngles implements ClampedItemPropertyFunction {
    private final Function<ItemStack, BlockPos> targetGetter;
    private double rotation;
    private double rota;
    private long lastUpdateTick;

    public CompassAngles(Function<ItemStack, BlockPos> targetGetter) {
        this.targetGetter = targetGetter;
    }

    @Override
    public float unclampedCall(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (entity == null && !stack.isFramed()) {
            return 0.0F;
        }
        boolean held = entity != null;
        Entity compassEntity = held ? entity : stack.getFrame();
        if (compassEntity == null) {
            return 0.0F;
        }
        if (level == null && compassEntity.level() instanceof ClientLevel clientLevel) {
            level = clientLevel;
        }
        BlockPos target = targetGetter.apply(stack);
        double angle;
        if (target != null && level != null) {
            double entityRotation = held ? compassEntity.getYRot() : getFrameRotation((ItemFrame) compassEntity);
            entityRotation = entityRotation % 360.0D;
            double angleToPos = Math.atan2(target.getZ() - compassEntity.getZ(), target.getX() - compassEntity.getX());
            angle = Math.PI - ((entityRotation - 90.0D) * (Math.PI / 180.0) - angleToPos);
        } else {
            angle = Math.random() * (Math.PI * 2D);
            if (held && level != null) {
                angle = wobble(level, angle);
            }
        }
        return Mth.positiveModulo((float) (angle / (Math.PI * 2D)), 1.0F);
    }

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

    private static double getFrameRotation(ItemFrame itemFrame) {
        return Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
    }
}
