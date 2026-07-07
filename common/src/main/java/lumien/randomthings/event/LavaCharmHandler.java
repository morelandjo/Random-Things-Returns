package lumien.randomthings.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import lumien.randomthings.item.LavaCharmItem;
import lumien.randomthings.item.LavaWadersItem;
import lumien.randomthings.item.ObsidianWaterWalkingBootsItem;
import lumien.randomthings.item.ObsidianSkullItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Lava Charm behaviour: recharge on each player tick, and negate lava damage (spending a charge)
 * via {@link EntityEvent#LIVING_HURT}.
 */
public final class LavaCharmHandler {

    private LavaCharmHandler() {
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(LavaCharmHandler::onPlayerTick);
        EntityEvent.LIVING_HURT.register(LavaCharmHandler::onLivingHurt);
    }

    private static void onPlayerTick(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        ItemStack charm = findCharm(player);
        if (!charm.isEmpty()) {
            LavaCharmItem.tickCharge(charm);
        }
    }

    private static EventResult onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof ServerPlayer player)) {
            return EventResult.pass();
        }
        if (!source.is(DamageTypeTags.IS_FIRE)) {
            return EventResult.pass();
        }

        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);

        if ("lava".equals(source.getMsgId())) {
            // Lava damage: Lava Waders boots take priority over a Lava Charm in the inventory.
            if (boots.getItem() instanceof LavaWadersItem && LavaCharmItem.getCharge(boots) > 0) {
                LavaCharmItem.useCharge(boots);
                return EventResult.interruptFalse();
            }
            ItemStack charm = findCharm(player);
            if (!charm.isEmpty() && LavaCharmItem.getCharge(charm) > 0) {
                LavaCharmItem.useCharge(charm);
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        }

        // Other fire damage: Obsidian Skull (or Lava Waders) gives a chance to negate, weighted so
        // small ticks are almost always blocked. chance = (amount/100) * amount^2.
        boolean fireProtection = boots.getItem() instanceof LavaWadersItem
            || boots.getItem() instanceof ObsidianWaterWalkingBootsItem
            || !findItem(player, ObsidianSkullItem.class).isEmpty();
        if (fireProtection) {
            float chance = (amount / 100.0F) * amount * amount;
            if (player.getRandom().nextFloat() > chance) {
                return EventResult.interruptFalse();
            }
        }
        return EventResult.pass();
    }

    private static ItemStack findCharm(Player player) {
        return findItem(player, LavaCharmItem.class);
    }

    private static ItemStack findItem(Player player, Class<?> itemType) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (itemType.isInstance(stack.getItem())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
