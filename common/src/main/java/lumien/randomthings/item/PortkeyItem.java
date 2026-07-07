package lumien.randomthings.item;

import lumien.randomthings.util.PortkeyTarget;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Portkey — right-click a block to bind a destination; drop it and let it sit for 5s (100 ticks),
 * then pick it up to teleport near the target. Aging + pickup-teleport are driven by the common
 * {@code ItemEntityMixin} (vanilla has no cross-loader item-pickup / item-entity-tick event).
 */
public class PortkeyItem extends Item {

    public PortkeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            ItemStack stack = context.getItemInHand();
            PortkeyTarget target = new PortkeyTarget(context.getLevel().dimension(), context.getClickedPos());
            RTNbt.setCodec(stack, RTDataKeys.PORTKEY_TARGET, PortkeyTarget.CODEC, target);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Only ages while on the ground (see the mixin); reset the counter while carried.
        RTNbt.remove(stack, RTDataKeys.PORTKEY_AGE);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if (RTNbt.has(stack, RTDataKeys.PORTKEY_AGE) && RTNbt.getInt(stack, RTDataKeys.PORTKEY_AGE) < 100) {
            return true; // glint while priming
        }
        return getTarget(stack).isEmpty(); // glint until a destination is bound
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Optional<PortkeyTarget> target = getTarget(stack);
        if (target.isPresent()) {
            BlockPos pos = target.get().pos();
            tooltip.add(Component.translatable("item.randomthings.portkey.x", pos.getX()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.randomthings.portkey.y", pos.getY()).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.randomthings.portkey.z", pos.getZ()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.randomthings.portkey.notset").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        Optional<PortkeyTarget> target = getTarget(stack);
        if (target.isPresent()) {
            BlockPos pos = target.get().pos();
            return Component.literal(baseName.getString() + String.format(" (%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ()));
        }
        return baseName;
    }

    public static Optional<PortkeyTarget> getTarget(ItemStack stack) {
        return RTNbt.getCodec(stack, RTDataKeys.PORTKEY_TARGET, PortkeyTarget.CODEC);
    }

    @Nullable
    public static ResourceLocation getCamo(ItemStack stack) {
        return RTNbt.getResourceLocation(stack, RTDataKeys.PORTKEY_CAMO);
    }
}
