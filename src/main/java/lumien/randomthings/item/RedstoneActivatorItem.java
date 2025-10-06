package lumien.randomthings.item;

import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class RedstoneActivatorItem extends Item {
    private static final int[] DURATIONS = new int[] { 2, 20, 100 };

    public RedstoneActivatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        int duration = DURATIONS[getDurationIndex(stack)];
        tooltip.add(Component.translatable("tooltip.randomthings.redstone_activator.duration", duration)
            .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        int currentDurationIndex = getDurationIndex(stack);
        int nextDurationIndex;

        if (player.isShiftKeyDown()) {
            nextDurationIndex = currentDurationIndex - 1;
            nextDurationIndex = nextDurationIndex < 0 ? DURATIONS.length - 1 : nextDurationIndex;
        } else {
            nextDurationIndex = currentDurationIndex + 1;
            nextDurationIndex = nextDurationIndex >= DURATIONS.length ? 0 : nextDurationIndex;
        }

        setDurationIndex(stack, nextDurationIndex);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // This method has priority over block interactions
        Level level = context.getLevel();

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            RedstoneSignalHandler handler = RedstoneSignalHandler.get(serverLevel.getServer());
            int duration = DURATIONS[getDurationIndex(stack)];
            handler.addSignal(level, context.getClickedPos(), duration, 15);

            return InteractionResult.SUCCESS;
        }

        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) {
            return true;
        }
        // Don't show animation when just changing duration
        return oldStack.getItem() != newStack.getItem();
    }

    public int getDurationIndex(ItemStack stack) {
        Integer durationIndex = stack.get(ModDataComponents.REDSTONE_ACTIVATOR_DURATION.get());
        return durationIndex != null ? durationIndex : 1; // Default to index 1 (20 ticks)
    }

    public void setDurationIndex(ItemStack stack, int index) {
        stack.set(ModDataComponents.REDSTONE_ACTIVATOR_DURATION.get(), index);
    }
}
