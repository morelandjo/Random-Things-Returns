package lumien.randomthings.item;

import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

/**
 * Custom armor materials (1.20.1 interface form). The "indestructible" boots use a very high
 * durability rather than the 1.20.5 {@code durability(0)} convention.
 */
public final class ModArmorMaterials {

    private static final int INDESTRUCTIBLE = 100_000;

    private static EnumMap<ArmorItem.Type, Integer> chainDefense() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, 1);
            m.put(ArmorItem.Type.LEGGINGS, 4);
            m.put(ArmorItem.Type.CHESTPLATE, 5);
            m.put(ArmorItem.Type.HELMET, 2);
        });
    }

    private static EnumMap<ArmorItem.Type, Integer> ironDefense() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
            m.put(ArmorItem.Type.BOOTS, 2);
            m.put(ArmorItem.Type.LEGGINGS, 5);
            m.put(ArmorItem.Type.CHESTPLATE, 6);
            m.put(ArmorItem.Type.HELMET, 2);
        });
    }

    public static final ArmorMaterial WATER_WALKING = new RTArmorMaterial(
        "randomthings:water_walking_boots", INDESTRUCTIBLE, chainDefense(), 15,
        SoundEvents.ARMOR_EQUIP_CHAIN, () -> Ingredient.of(Items.IRON_INGOT), 0.0F, 0.0F);

    public static final ArmorMaterial LAVA_WADERS = new RTArmorMaterial(
        "randomthings:lava_waders", INDESTRUCTIBLE, chainDefense(), 15,
        SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT), 0.0F, 0.0F);

    public static final ArmorMaterial SUPER_LUBRICENT = new RTArmorMaterial(
        "randomthings:superlubricentboots", 195, ironDefense(), 9,
        SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(Items.IRON_INGOT), 0.0F, 0.0F);

    public static final ArmorMaterial MAGIC_HOOD = new RTArmorMaterial(
        "randomthings:magic_hood", INDESTRUCTIBLE, chainDefense(), 15,
        SoundEvents.ARMOR_EQUIP_CHAIN, () -> Ingredient.of(Items.LEATHER), 0.0F, 0.0F);

    public static final ArmorMaterial OBSIDIAN_WATER_WALKING = new RTArmorMaterial(
        "randomthings:obsidian_water_walking_boots", INDESTRUCTIBLE, chainDefense(), 15,
        SoundEvents.ARMOR_EQUIP_CHAIN, () -> Ingredient.of(Items.OBSIDIAN), 0.0F, 0.0F);

    private ModArmorMaterials() {
    }
}
