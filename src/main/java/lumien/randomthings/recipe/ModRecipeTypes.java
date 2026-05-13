package lumien.randomthings.recipe;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, ModConstants.MOD_ID);

    public static final Supplier<RecipeType<DyeingMachineRecipe>> DYEING_MACHINE =
        RECIPE_TYPES.register("dyeing_machine", () -> new RecipeType<DyeingMachineRecipe>() {
            @Override
            public String toString() {
                return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "dyeing_machine").toString();
            }
        });

    public static final Supplier<RecipeType<ImbuingRecipe>> IMBUING =
        RECIPE_TYPES.register("imbuing", () -> new RecipeType<ImbuingRecipe>() {
            @Override
            public String toString() {
                return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "imbuing").toString();
            }
        });
}
