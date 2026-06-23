package lumien.randomthings.item;

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

import java.util.List;

/**
 * Holds up to 128 Floo Powder. Sneak-right-click anywhere to refill from the player's inventory.
 * The pouch is consumed 1 charge per teleport (see ChatEventHandler floo branch).
 *
 * Mirrors upstream ItemFlooPouch.java.
 */
public class FlooPouchItem extends Item {
    public static final int MAX_CHARGE = 128;

    public FlooPouchItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.UNCOMMON)
            .component(ModDataComponents.FLOO_POUCH_CHARGE.get(), 0)
        );
    }

    public static int getCharge(ItemStack stack) {
        Integer c = stack.get(ModDataComponents.FLOO_POUCH_CHARGE.get());
        return c == null ? 0 : c;
    }

    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.FLOO_POUCH_CHARGE.get(), Math.max(0, Math.min(charge, MAX_CHARGE)));
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
        // green
        return 0x33CC33;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.floo_pouch.charge", getCharge(stack), MAX_CHARGE).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.randomthings.floo_pouch").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
