package lumien.randomthings.item;

import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.StableEnderPearlEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class StableEnderPearlItem extends Item {

    public StableEnderPearlItem() {
        super(new Item.Properties());
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Bind the pearl to the player
            itemStack.set(ModDataComponents.PLAYER_UUID.get(), player.getUUID());
            player.sendSystemMessage(Component.translatable("item.randomthings.stable_ender_pearl.bound"));
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        UUID boundPlayer = stack.get(ModDataComponents.PLAYER_UUID.get());
        if (boundPlayer != null) {
            return Component.translatable("item.randomthings.stable_ender_pearl.bound.name");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        UUID boundPlayer = stack.get(ModDataComponents.PLAYER_UUID.get());
        if (boundPlayer != null) {
            tooltip.add(Component.translatable("item.randomthings.stable_ender_pearl.bound.info"));
        } else {
            tooltip.add(Component.translatable("item.randomthings.stable_ender_pearl.info"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    @Nullable
    public Entity createEntity(Level level, Entity location, ItemStack stack) {
        return new StableEnderPearlEntity(level, location.getX(), location.getY(), location.getZ(), stack);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }
}