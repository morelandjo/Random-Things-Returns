package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record DyeingMachineRecipe(Ingredient input, Ingredient dye, ItemStack result) implements Recipe<DyeingMachineRecipe.Input> {

    public static final MapCodec<DyeingMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(DyeingMachineRecipe::input),
            Ingredient.CODEC.fieldOf("dye").forGetter(DyeingMachineRecipe::dye),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DyeingMachineRecipe::result)
        ).apply(instance, DyeingMachineRecipe::new)
    );

    public record Input(ItemStack inputStack, ItemStack dyeStack) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return switch (slot) {
                case 0 -> inputStack;
                case 1 -> dyeStack;
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int size() {
            return 2;
        }
    }

    @Override
    public boolean matches(Input input, Level level) {
        return this.input.test(input.inputStack()) && this.dye.test(input.dyeStack());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(this.input);
        list.add(this.dye);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DYEING_MACHINE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.DYEING_MACHINE.get();
    }
}
