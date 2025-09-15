package lumien.randomthings.recipe;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER, ModConstants.MOD_ID);

    public static final Supplier<RecipeSerializer<DiaphanousBlockRecipe>> DIAPHANOUS_BLOCK = 
        RECIPE_SERIALIZERS.register("diaphanous_block", DiaphanousBlockRecipeSerializer::new);
        
    public static final Supplier<RecipeSerializer<EmeraldCompassRecipe>> EMERALD_COMPASS = 
        RECIPE_SERIALIZERS.register("emerald_compass", EmeraldCompassRecipeSerializer::new);
}