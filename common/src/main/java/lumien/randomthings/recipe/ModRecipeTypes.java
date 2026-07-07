package lumien.randomthings.recipe;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public final class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.RECIPE_TYPE);

    public static final RegistrySupplier<RecipeType<DyeingMachineRecipe>> DYEING_MACHINE =
        RECIPE_TYPES.register("dyeing_machine", () -> new RecipeType<DyeingMachineRecipe>() {
            @Override
            public String toString() {
                return new ResourceLocation(ModConstants.MOD_ID, "dyeing_machine").toString();
            }
        });

    public static final RegistrySupplier<RecipeType<ImbuingRecipe>> IMBUING =
        RECIPE_TYPES.register("imbuing", () -> new RecipeType<ImbuingRecipe>() {
            @Override
            public String toString() {
                return new ResourceLocation(ModConstants.MOD_ID, "imbuing").toString();
            }
        });

    private ModRecipeTypes() {
    }

    public static void register() {
        RECIPE_TYPES.register();
    }
}
