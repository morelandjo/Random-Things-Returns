package lumien.randomthings.item;

import java.util.List;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Right-click to bind to yourself; toss it and 7 seconds later it teleports the bound player
 * (or a random nearby entity) to where it landed. Countdown runs in
 * {@link lumien.randomthings.event.StableEnderPearlHandler} via the ItemEntity mixin.
 */
public class StableEnderPearlItem extends Item {

    public StableEnderPearlItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            RTNbt.setUUID(itemStack, RTDataKeys.PLAYER_UUID, player.getUUID());
            player.sendSystemMessage(Component.translatable("item.randomthings.stable_ender_pearl.bound"));
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (RTNbt.getUUID(stack, RTDataKeys.PLAYER_UUID) != null) {
            return Component.translatable("item.randomthings.stable_ender_pearl.bound.name");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (RTNbt.getUUID(stack, RTDataKeys.PLAYER_UUID) != null) {
            tooltip.add(Component.translatable("item.randomthings.stable_ender_pearl.bound.info"));
        } else {
            tooltip.add(Component.translatable("item.randomthings.stable_ender_pearl.info"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
