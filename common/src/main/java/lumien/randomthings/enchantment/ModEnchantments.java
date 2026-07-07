package lumien.randomthings.enchantment;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantment registry. 1.20.1 enchantments are code-based (registered into
 * {@code Registries.ENCHANTMENT}), unlike the data-driven 1.21 form.
 */
public final class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.ENCHANTMENT);

    public static final RegistrySupplier<Enchantment> MAGNETIC =
        ENCHANTMENTS.register("magnetic", MagneticEnchantment::new);

    private ModEnchantments() {
    }

    public static void register() {
        ENCHANTMENTS.register();
    }
}
