package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Holds up to 128 Floo Powder. Sneak-right-click to refill from the player's inventory; consumed
 * 1 charge per teleport (see {@code ChatEventHandler} floo branch).
 */
public class FlooPouchItem extends Item {
    public static final int MAX_CHARGE = 128;

    public FlooPouchItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public static int getCharge(ItemStack stack) {
        return RTNbt.getInt(stack, RTDataKeys.FLOO_POUCH_CHARGE, 0);
    }

    public static void setCharge(ItemStack stack, int charge) {
        RTNbt.setInt(stack, RTDataKeys.FLOO_POUCH_CHARGE, Math.max(0, Math.min(charge, MAX_CHARGE)));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(pouch);
        }
        if (level.isClientSide) {
            return InteractionResultHolder.success(pouch);
        }

        int charge = getCharge(pouch);
        int capacity = MAX_CHARGE - charge;
        if (capacity <= 0) {
            return InteractionResultHolder.pass(pouch);
        }

        int absorbed = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.is(ModItems.FLOO_POWDER.get())) {
                int take = Math.min(invStack.getCount(), capacity - absorbed);
                if (take > 0) {
                    invStack.shrink(take);
                    absorbed += take;
                    if (absorbed >= capacity) break;
                }
            }
        }
        if (absorbed > 0) {
            setCharge(pouch, charge + absorbed);
            return InteractionResultHolder.success(pouch);
        }
        return InteractionResultHolder.pass(pouch);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCharge(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getCharge(stack) / (float) MAX_CHARGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x33CC33;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.floo_pouch.charge", getCharge(stack), MAX_CHARGE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.randomthings.floo_pouch").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
