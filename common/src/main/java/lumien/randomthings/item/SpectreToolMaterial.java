package lumien.randomthings.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Spectre tool tier: diamond harvest level, higher durability/enchantability, repaired with Ectoplasm. */
public final class SpectreToolMaterial {

    public static final Tier SPECTRE = new Tier() {
        @Override
        public int getUses() {
            return 2000;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
        }

        @Override
        public int getLevel() {
            return 3; // diamond-equivalent harvest level
        }

        @Override
        public int getEnchantmentValue() {
            return 22;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.ECTOPLASM.get());
        }
    };

    private SpectreToolMaterial() {
    }
}
