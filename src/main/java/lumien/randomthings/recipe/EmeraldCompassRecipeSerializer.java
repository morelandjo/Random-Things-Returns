package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EmeraldCompassRecipeSerializer implements RecipeSerializer<EmeraldCompassRecipe> {
    
    public static final MapCodec<EmeraldCompassRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(EmeraldCompassRecipe::getId)
        ).apply(instance, EmeraldCompassRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EmeraldCompassRecipe> STREAM_CODEC = 
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, EmeraldCompassRecipe::getId,
            EmeraldCompassRecipe::new
        );

    @Override
    public MapCodec<EmeraldCompassRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EmeraldCompassRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}