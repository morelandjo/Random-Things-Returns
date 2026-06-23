package lumien.randomthings.item;

import lumien.randomthings.entity.TemporaryFlooFireplaceEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * When thrown on the ground and left to settle for >100 ticks, spawns a temporary fireplace
 * entity at the token's position (unless another fireplace is already within 5 blocks).
 * Mirrors upstream ItemFlooToken.java:24-50.
 */
public class FlooTokenItem extends Item {
    private static final int ARM_AGE = 100;
    private static final double RADIUS_CHECK = 5.0;

    public FlooTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Reset age while in an inventory
        stack.remove(ModDataComponents.FLOO_TOKEN_AGE.get());
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide) {
            return false;
        }
        if (!entity.onGround()) {
            return false;
        }

        Integer ageBox = stack.get(ModDataComponents.FLOO_TOKEN_AGE.get());
        int age = ageBox == null ? 0 : ageBox;

        if (age >= ARM_AGE) {
            AABB area = entity.getBoundingBox().inflate(RADIUS_CHECK);
            List<TemporaryFlooFireplaceEntity> nearby = entity.level().getEntitiesOfClass(TemporaryFlooFireplaceEntity.class, area);
            if (nearby.isEmpty()) {
                TemporaryFlooFireplaceEntity tempFire = new TemporaryFlooFireplaceEntity(
                    entity.level(), entity.getX(), entity.getY(), entity.getZ());
                entity.level().addFreshEntity(tempFire);
                entity.discard();
                return true;
            }
            // Wait — another fireplace too close
            return false;
        }

        stack.set(ModDataComponents.FLOO_TOKEN_AGE.get(), age + 1);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.floo_token").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
