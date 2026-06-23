package lumien.randomthings.enchantment;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Holder constants for the mod's data-driven enchantments. 1.21 enchantments are pure data
 * (defined in `data/randomthings/enchantment/*.json`); the Java side only needs a ResourceKey
 * for lookup.
 */
public final class ModEnchantments {
    private ModEnchantments() {}

    public static final ResourceKey<Enchantment> MAGNETIC = ResourceKey.create(
        Registries.ENCHANTMENT,
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "magnetic")
    );
}
