package lumien.randomthings.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ImbuingRecipeSerializer implements RecipeSerializer<ImbuingRecipe> {

    @Override
    public ImbuingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient i1 = Ingredient.fromJson(GsonHelper.getNonNull(json, "ingredient1"));
        Ingredient i2 = Ingredient.fromJson(GsonHelper.getNonNull(json, "ingredient2"));
        Ingredient i3 = Ingredient.fromJson(GsonHelper.getNonNull(json, "ingredient3"));
        Ingredient centre = Ingredient.fromJson(GsonHelper.getNonNull(json, "center"));

        JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(resultObj, "item"));
        int count = GsonHelper.getAsInt(resultObj, "count", 1);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(itemId), count);

        return new ImbuingRecipe(recipeId, i1, i2, i3, centre, result);
    }

    @Override
    public ImbuingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
        Ingredient i1 = Ingredient.fromNetwork(buf);
        Ingredient i2 = Ingredient.fromNetwork(buf);
        Ingredient i3 = Ingredient.fromNetwork(buf);
        Ingredient centre = Ingredient.fromNetwork(buf);
        ItemStack result = buf.readItem();
        return new ImbuingRecipe(recipeId, i1, i2, i3, centre, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ImbuingRecipe recipe) {
        recipe.ingredient1().toNetwork(buf);
        recipe.ingredient2().toNetwork(buf);
        recipe.ingredient3().toNetwork(buf);
        recipe.toImbue().toNetwork(buf);
        buf.writeItem(recipe.result());
    }
}
