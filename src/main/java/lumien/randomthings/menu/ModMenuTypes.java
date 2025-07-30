package lumien.randomthings.menu;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(BuiltInRegistries.MENU, ModConstants.MOD_ID);

    public static final Supplier<MenuType<AdvancedRedstoneTorchMenu>> ADVANCED_REDSTONE_TORCH = 
        MENU_TYPES.register("advanced_redstone_torch", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new AdvancedRedstoneTorchMenu(windowId, ContainerLevelAccess.NULL)));
}