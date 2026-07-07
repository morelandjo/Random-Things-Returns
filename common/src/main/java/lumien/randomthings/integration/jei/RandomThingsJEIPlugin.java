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
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI integration for Random Things' workstations. Lives in the common module and is shadowed into
 * both platform jars, so JEI's {@code @JeiPlugin} scan finds it on Fabric and Forge.
 */
@JeiPlugin
public class RandomThingsJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ModConstants.MOD_ID, "jei_plugin");
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
        List<DyeingMachineRecipe> dyeing = mc.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.DYEING_MACHINE.get());
        registration.addRecipes(DyeingMachineCategory.RECIPE_TYPE, dyeing);

        List<ImbuingRecipe> imbuing = mc.level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.IMBUING.get());
        registration.addRecipes(ImbuingCategory.RECIPE_TYPE, imbuing);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.DYEING_MACHINE.get()), DyeingMachineCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.IMBUING_STATION.get()), ImbuingCategory.RECIPE_TYPE);
    }
}
