package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ImbuingRecipeSerializer implements RecipeSerializer<ImbuingRecipe> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ImbuingRecipe> STREAM_CODEC = StreamCodec.of(
        ImbuingRecipeSerializer::toNetwork,
        ImbuingRecipeSerializer::fromNetwork
    );

    @Override
    public MapCodec<ImbuingRecipe> codec() {
        return ImbuingRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ImbuingRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, ImbuingRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient1());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient2());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.ingredient3());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.toImbue());
        ItemStack.STREAM_CODEC.encode(buf, recipe.result());
    }

    private static ImbuingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        Ingredient i1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient i2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient i3 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient centre = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
        return new ImbuingRecipe(i1, i2, i3, centre, result);
    }
}
