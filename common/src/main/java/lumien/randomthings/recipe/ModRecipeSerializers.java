package lumien.randomthings.recipe;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<DyeingMachineRecipe>> DYEING_MACHINE =
        RECIPE_SERIALIZERS.register("dyeing_machine", DyeingMachineRecipeSerializer::new);

    public static final RegistrySupplier<RecipeSerializer<ImbuingRecipe>> IMBUING =
        RECIPE_SERIALIZERS.register("imbuing", ImbuingRecipeSerializer::new);

    public static final RegistrySupplier<RecipeSerializer<EmeraldCompassRecipe>> EMERALD_COMPASS =
        RECIPE_SERIALIZERS.register("emerald_compass",
            () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(EmeraldCompassRecipe::new));

    public static final RegistrySupplier<RecipeSerializer<GoldenCompassRecipe>> GOLDEN_COMPASS =
        RECIPE_SERIALIZERS.register("golden_compass",
            () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(GoldenCompassRecipe::new));

    public static final RegistrySupplier<RecipeSerializer<DiaphanousBlockRecipe>> DIAPHANOUS_BLOCK =
        RECIPE_SERIALIZERS.register("diaphanous_block",
            () -> new net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer<>(DiaphanousBlockRecipe::new));

    private ModRecipeSerializers() {
    }

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}
