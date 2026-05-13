package lumien.randomthings.integration.jei;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.recipe.DyeingMachineRecipe;
import lumien.randomthings.recipe.ImbuingRecipe;
import lumien.randomthings.recipe.ModRecipeTypes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

@JeiPlugin
public class RandomThingsJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DyeingMachineCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ImbuingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;
        List<RecipeHolder<DyeingMachineRecipe>> dyeingRecipes = mc.level.getRecipeManager()
            .getAllRecipesFor(ModRecipeTypes.DYEING_MACHINE.get());
        registration.addRecipes(DyeingMachineCategory.RECIPE_TYPE, dyeingRecipes);

        List<RecipeHolder<ImbuingRecipe>> imbuingRecipes = mc.level.getRecipeManager()
            .getAllRecipesFor(ModRecipeTypes.IMBUING.get());
        registration.addRecipes(ImbuingCategory.RECIPE_TYPE, imbuingRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DYEING_MACHINE.get()), DyeingMachineCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.IMBUING_STATION.get()), ImbuingCategory.RECIPE_TYPE);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }
}
