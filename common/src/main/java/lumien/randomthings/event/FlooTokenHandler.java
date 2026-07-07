package lumien.randomthings.event;

import lumien.randomthings.entity.TemporaryFlooFireplaceEntity;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Floo Token ground behaviour, invoked from {@code ItemEntityMixin#tick}: once a thrown token has
 * settled for {@link #ARM_AGE} ticks (and no fireplace is nearby) it becomes a Temporary Fireplace.
 */
public final class FlooTokenHandler {

    private static final int ARM_AGE = 100;
    private static final double RADIUS_CHECK = 5.0;

    private FlooTokenHandler() {
    }

    public static void tick(ItemEntity entity) {
        if (entity.level().isClientSide || !entity.onGround()) {
            return;
        }
        ItemStack stack = entity.getItem();
        if (stack.getItem() != ModItems.FLOO_TOKEN.get()) {
            return;
        }
        int age = RTNbt.getInt(stack, RTDataKeys.FLOO_TOKEN_AGE, 0);
        if (age < ARM_AGE) {
            RTNbt.setInt(stack, RTDataKeys.FLOO_TOKEN_AGE, age + 1);
            return;
        }

        AABB area = entity.getBoundingBox().inflate(RADIUS_CHECK);
        List<TemporaryFlooFireplaceEntity> nearby =
            entity.level().getEntitiesOfClass(TemporaryFlooFireplaceEntity.class, area);
        if (nearby.isEmpty()) {
            TemporaryFlooFireplaceEntity fire =
                new TemporaryFlooFireplaceEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ());
            entity.level().addFreshEntity(fire);
            entity.discard();
        }
    }
}
