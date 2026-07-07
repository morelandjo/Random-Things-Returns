package lumien.randomthings.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * A 1.20.1 {@link ArmorMaterial} implementation (the interface form). 1.20.5 replaced this with a
 * registry-backed record + {@code Holder}; here we just implement the interface directly.
 */
public final class RTArmorMaterial implements ArmorMaterial {
    private final String name;
    private final int durability;
    private final EnumMap<ArmorItem.Type, Integer> defense;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final Supplier<Ingredient> repairIngredient;
    private final float toughness;
    private final float knockbackResistance;

    public RTArmorMaterial(String name, int durability, EnumMap<ArmorItem.Type, Integer> defense,
                           int enchantmentValue, SoundEvent equipSound, Supplier<Ingredient> repairIngredient,
                           float toughness, float knockbackResistance) {
        this.name = name;
        this.durability = durability;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.repairIngredient = repairIngredient;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return durability;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defense.getOrDefault(type, 0);
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
