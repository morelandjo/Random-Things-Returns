package lumien.randomthings.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DyeingMachineRecipeSerializer implements RecipeSerializer<DyeingMachineRecipe> {

    @Override
    public DyeingMachineRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(json, "input"));
        Ingredient dye = Ingredient.fromJson(GsonHelper.getNonNull(json, "dye"));

        JsonObject resultObj = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(resultObj, "item"));
        int count = GsonHelper.getAsInt(resultObj, "count", 1);
        ItemStack result = new ItemStack(BuiltInRegistries.ITEM.get(itemId), count);

        return new DyeingMachineRecipe(recipeId, input, dye, result);
    }

    @Override
    public DyeingMachineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buf) {
        Ingredient input = Ingredient.fromNetwork(buf);
        Ingredient dye = Ingredient.fromNetwork(buf);
        ItemStack result = buf.readItem();
        return new DyeingMachineRecipe(recipeId, input, dye, result);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, DyeingMachineRecipe recipe) {
        recipe.input().toNetwork(buf);
        recipe.dye().toNetwork(buf);
        buf.writeItem(recipe.result());
    }
}
