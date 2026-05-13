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
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * JEI category for the Imbuing Station. The slot layout mirrors upstream
 * {@code ImbuingRecipeCategory}: three ingredient slots arranged top/left/bottom around
 * a central "to-imbue" slot, with the output displayed to the right of centre.
 */
public class ImbuingCategory implements IRecipeCategory<RecipeHolder<ImbuingRecipe>> {

    public static final ResourceLocation UID =
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "imbuing");

    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/gui/imbuing_station.png");

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final RecipeType<RecipeHolder<ImbuingRecipe>> RECIPE_TYPE =
        new RecipeType<>(UID, (Class) RecipeHolder.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ImbuingCategory(IGuiHelper guiHelper) {
        // Crop the slot-area of the GUI texture: 112x112 starting at (32, 6).
        this.background = guiHelper.createDrawable(TEXTURE, 32, 6, 112, 112);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.IMBUING_STATION.get()));
    }

    @Override
    public RecipeType<RecipeHolder<ImbuingRecipe>> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<ImbuingRecipe> holder, IFocusGroup focuses) {
        ImbuingRecipe recipe = holder.value();
        // Slot coordinates relative to the 112x112 cropped background, matching upstream JEI layout.
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 1).addIngredients(recipe.ingredient1());
        builder.addSlot(RecipeIngredientRole.INPUT, 3, 46).addIngredients(recipe.ingredient2());
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 91).addIngredients(recipe.ingredient3());
        builder.addSlot(RecipeIngredientRole.INPUT, 48, 46).addIngredients(recipe.toImbue());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 93, 46).addItemStack(recipe.result());
    }
}
