package lumien.randomthings.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/**
 * Magnetic — a digging-tool enchantment that sends broken-block drops straight into the breaker's
 * inventory (see the {@code BlockEvent.BREAK} handler in {@code RTEvents}).
 *
 * <p>1.20.1 enchantments are code-defined (the 1.21.1 data-driven JSON form does not apply here).</p>
 */
public class MagneticEnchantment extends Enchantment {

    public MagneticEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }

    @Override
    public int getMaxCost(int level) {
        return 65;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
