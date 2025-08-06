package lumien.randomthings.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DiaphanousBlockRecipeSerializer implements RecipeSerializer<DiaphanousBlockRecipe> {
    
    public static final MapCodec<DiaphanousBlockRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(DiaphanousBlockRecipe::getId)
        ).apply(instance, DiaphanousBlockRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DiaphanousBlockRecipe> STREAM_CODEC = 
        StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, DiaphanousBlockRecipe::getId,
            DiaphanousBlockRecipe::new
        );

    @Override
    public MapCodec<DiaphanousBlockRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DiaphanousBlockRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}