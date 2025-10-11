package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpectreAnchorRecipeSerializer implements RecipeSerializer<SpectreAnchorRecipe> {

    public static final MapCodec<SpectreAnchorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(SpectreAnchorRecipe::getId)
        ).apply(instance, SpectreAnchorRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SpectreAnchorRecipe> STREAM_CODEC =
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SpectreAnchorRecipe::getId,
            SpectreAnchorRecipe::new
        );

    @Override
    public MapCodec<SpectreAnchorRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, SpectreAnchorRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
