package lumien.randomthings.item;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Defines custom armor materials for Random Things armor items
 */
public class ModArmorMaterials {

    /**
     * Water Walking Boots armor material
     * - Chain armor stats (same protection as chain boots)
     * - Indestructible (0 durability multiplier)
     * - Rare rarity
     */
    public static final Holder<ArmorMaterial> WATER_WALKING = register(
        "water_walking",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            // Defense values per armor type (same as chain armor)
            map.put(ArmorItem.Type.BOOTS, 1);
            map.put(ArmorItem.Type.LEGGINGS, 4);
            map.put(ArmorItem.Type.CHESTPLATE, 5);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 4);
        }),
        15, // Enchantability (same as chain)
        SoundEvents.ARMOR_EQUIP_CHAIN,
        () -> Ingredient.of(Items.IRON_INGOT),
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "water_walking_boots")
            )
        ),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    /**
     * Obsidian Water Walking Boots armor material
     * - Same stats as water walking boots
     * - Also provides fire protection
     */
    public static final Holder<ArmorMaterial> OBSIDIAN_WATER_WALKING = register(
        "obsidian_water_walking",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            // Defense values per armor type (same as chain armor)
            map.put(ArmorItem.Type.BOOTS, 1);
            map.put(ArmorItem.Type.LEGGINGS, 4);
            map.put(ArmorItem.Type.CHESTPLATE, 5);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 4);
        }),
        15, // Enchantability (same as chain)
        SoundEvents.ARMOR_EQUIP_CHAIN,
        () -> Ingredient.of(Items.OBSIDIAN),
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "obsidian_water_walking_boots")
            )
        ),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    /**
     * Lava Waders armor material
     * - Combines all effects: water walking, fire protection, lava immunity
     * - Ultimate boots with charge system
     */
    public static final Holder<ArmorMaterial> LAVA_WADERS = register(
        "lava_waders",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            // Defense values per armor type (same as chain armor)
            map.put(ArmorItem.Type.BOOTS, 1);
            map.put(ArmorItem.Type.LEGGINGS, 4);
            map.put(ArmorItem.Type.CHESTPLATE, 5);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 4);
        }),
        15, // Enchantability (same as chain)
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        () -> Ingredient.of(Items.NETHERITE_INGOT),
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "lava_waders")
            )
        ),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    /**
     * Magic Hood armor material
     * - Chain armor protection level
     * - Indestructible (no durability)
     * - Hides nameplate and potion particles
     * - Found in dungeon and blacksmith chests
     */
    public static final Holder<ArmorMaterial> MAGIC_HOOD = register(
        "magic_hood",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            // Defense values per armor type (same as chain armor)
            map.put(ArmorItem.Type.BOOTS, 1);
            map.put(ArmorItem.Type.LEGGINGS, 4);
            map.put(ArmorItem.Type.CHESTPLATE, 5);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 4);
        }),
        15, // Enchantability (same as chain)
        SoundEvents.ARMOR_EQUIP_CHAIN,
        () -> Ingredient.EMPTY, // Not repairable
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "magic_hood")
            )
        ),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    /**
     * Super Lubricent Boots armor material
     * - Iron armor stats (same protection as iron boots)
     * - Standard durability
     * - Provides frictionless movement when worn
     * - Can be enchanted
     */
    public static final Holder<ArmorMaterial> SUPER_LUBRICENT = register(
        "super_lubricent",
        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            // Defense values per armor type (same as iron armor)
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 5);
            map.put(ArmorItem.Type.CHESTPLATE, 6);
            map.put(ArmorItem.Type.HELMET, 2);
            map.put(ArmorItem.Type.BODY, 5);
        }),
        9, // Enchantability (same as iron)
        SoundEvents.ARMOR_EQUIP_IRON,
        () -> Ingredient.of(Items.IRON_INGOT),
        List.of(
            new ArmorMaterial.Layer(
                ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "superlubricentboots")
            )
        ),
        0.0F, // Toughness
        0.0F  // Knockback resistance
    );

    /**
     * Helper method to register an armor material
     */
    private static Holder<ArmorMaterial> register(
        String name,
        EnumMap<ArmorItem.Type, Integer> defense,
        int enchantmentValue,
        Holder<SoundEvent> equipSound,
        Supplier<Ingredient> repairIngredient,
        List<ArmorMaterial.Layer> layers,
        float toughness,
        float knockbackResistance
    ) {
        // Build material layers map
        EnumMap<ArmorItem.Type, Integer> typeMap = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values()) {
            typeMap.put(type, defense.get(type));
        }

        return Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, name),
            new ArmorMaterial(
                typeMap,
                enchantmentValue,
                equipSound,
                repairIngredient,
                layers,
                toughness,
                knockbackResistance
            )
        );
    }
}
