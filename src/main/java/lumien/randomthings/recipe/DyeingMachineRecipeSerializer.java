package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DyeingMachineRecipeSerializer implements RecipeSerializer<DyeingMachineRecipe> {

    public static final StreamCodec<RegistryFriendlyByteBuf, DyeingMachineRecipe> STREAM_CODEC = StreamCodec.of(
        DyeingMachineRecipeSerializer::toNetwork,
        DyeingMachineRecipeSerializer::fromNetwork
    );

    @Override
    public MapCodec<DyeingMachineRecipe> codec() {
        return DyeingMachineRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DyeingMachineRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, DyeingMachineRecipe recipe) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.dye());
        ItemStack.STREAM_CODEC.encode(buf, recipe.result());
    }

    private static DyeingMachineRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        Ingredient dye = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
        ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
        return new DyeingMachineRecipe(input, dye, result);
    }
}
