package lumien.randomthings.integration.jei;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.recipe.ImbuingRecipe;
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

/**
 * JEI category for the Imbuing Station: three ingredient slots around a central "to-imbue" slot,
 * with the output to the right of centre.
 */
public class ImbuingCategory implements IRecipeCategory<ImbuingRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(ModConstants.MOD_ID, "imbuing");
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModConstants.MOD_ID, "textures/gui/imbuing_station.png");

    public static final RecipeType<ImbuingRecipe> RECIPE_TYPE = new RecipeType<>(UID, ImbuingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ImbuingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 32, 6, 112, 112);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.IMBUING_STATION.get()));
    }

    @Override
    public RecipeType<ImbuingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.randomthings.imbuing_station");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ImbuingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 1).addIngredients(recipe.ingredient1());
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 46).addIngredients(recipe.ingredient2());
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 91).addIngredients(recipe.ingredient3());
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 46).addIngredients(recipe.toImbue());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 46).addItemStack(recipe.result());
    }
}
