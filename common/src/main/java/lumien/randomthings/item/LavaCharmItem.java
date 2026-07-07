package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Lava Charm — while carried, negates lava damage by spending charge; recharges when not in use.
 * (Protection itself is applied in {@code LavaCharmHandler} via {@code EntityEvent.LIVING_HURT}.)
 */
public class LavaCharmItem extends Item {

    public static final int MAX_CHARGE = 200; // ~10s of protection
    public static final int RECHARGE_COOLDOWN = 40; // 2s before recharging

    public LavaCharmItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    public static int getCharge(ItemStack stack) {
        // Absent NBT = a fresh, fully-charged charm.
        return RTNbt.getInt(stack, RTDataKeys.LAVA_CHARM_CHARGE, MAX_CHARGE);
    }

    public static void setCharge(ItemStack stack, int charge) {
        RTNbt.setInt(stack, RTDataKeys.LAVA_CHARM_CHARGE, Math.max(0, Math.min(charge, MAX_CHARGE)));
    }

    public static int getCooldown(ItemStack stack) {
        return RTNbt.getInt(stack, RTDataKeys.LAVA_CHARM_COOLDOWN, 0);
    }

    public static void setCooldown(ItemStack stack, int cooldown) {
        RTNbt.setInt(stack, RTDataKeys.LAVA_CHARM_COOLDOWN, Math.max(0, cooldown));
    }

    public static void useCharge(ItemStack stack) {
        int charge = getCharge(stack);
        if (charge > 0) {
            setCharge(stack, charge - 1);
            setCooldown(stack, RECHARGE_COOLDOWN);
        }
    }

    /** Recharge tick — call each tick while carried. */
    public static void tickCharge(ItemStack stack) {
        int cooldown = getCooldown(stack);
        if (cooldown > 0) {
            setCooldown(stack, cooldown - 1);
        } else {
            int charge = getCharge(stack);
            if (charge < MAX_CHARGE) {
                setCharge(stack, charge + 1);
            }
        }
    }
}
