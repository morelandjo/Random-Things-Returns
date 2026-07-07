package lumien.randomthings.integration.jei;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.recipe.DyeingMachineRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class DyeingMachineCategory implements IRecipeCategory<DyeingMachineRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(ModConstants.MOD_ID, "dyeing_machine");
    public static final RecipeType<DyeingMachineRecipe> RECIPE_TYPE = new RecipeType<>(UID, DyeingMachineRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public DyeingMachineCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 26);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DYEING_MACHINE.get()));
    }

    @Override
    public RecipeType<DyeingMachineRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.randomthings.dyeing_machine");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DyeingMachineRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 5).addIngredients(recipe.input());
        builder.addSlot(RecipeIngredientRole.INPUT, 27, 5).addIngredients(recipe.dye());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 5).addItemStack(recipe.result());
    }
}
