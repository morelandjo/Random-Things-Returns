package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class GoldenCompassRecipeSerializer implements RecipeSerializer<GoldenCompassRecipe> {

    public static final MapCodec<GoldenCompassRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(GoldenCompassRecipe::getId)
        ).apply(instance, GoldenCompassRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GoldenCompassRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GoldenCompassRecipe::getId,
            GoldenCompassRecipe::new
        );

    @Override
    public MapCodec<GoldenCompassRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GoldenCompassRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
