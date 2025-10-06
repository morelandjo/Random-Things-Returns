package lumien.randomthings.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class LavaCharmItem extends Item {

    public static final int MAX_CHARGE = 200; // 10 seconds worth of protection
    public static final int RECHARGE_COOLDOWN = 40; // 2 seconds before starting to recharge

    public LavaCharmItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.RARE)
            .component(ModDataComponents.LAVA_CHARM_CHARGE.get(), MAX_CHARGE)
            .component(ModDataComponents.LAVA_CHARM_COOLDOWN.get(), 0)
        );
    }

    /**
     * Get the current charge value from the item stack
     */
    public static int getCharge(ItemStack stack) {
        Integer charge = stack.get(ModDataComponents.LAVA_CHARM_CHARGE.get());
        return charge != null ? charge : 0;
    }

    /**
     * Set the charge value on the item stack
     */
    public static void setCharge(ItemStack stack, int charge) {
        stack.set(ModDataComponents.LAVA_CHARM_CHARGE.get(), Math.max(0, Math.min(charge, MAX_CHARGE)));
    }

    /**
     * Get the current cooldown value from the item stack
     */
    public static int getCooldown(ItemStack stack) {
        Integer cooldown = stack.get(ModDataComponents.LAVA_CHARM_COOLDOWN.get());
        return cooldown != null ? cooldown : 0;
    }

    /**
     * Set the cooldown value on the item stack
     */
    public static void setCooldown(ItemStack stack, int cooldown) {
        stack.set(ModDataComponents.LAVA_CHARM_COOLDOWN.get(), Math.max(0, cooldown));
    }

    /**
     * Use one charge from the Lava Charm and reset cooldown
     */
    public static void useCharge(ItemStack stack) {
        int charge = getCharge(stack);
        if (charge > 0) {
            setCharge(stack, charge - 1);
            setCooldown(stack, RECHARGE_COOLDOWN);
        }
    }

    /**
     * Update the Lava Charm's charge - call this every tick for players carrying the charm
     */
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
