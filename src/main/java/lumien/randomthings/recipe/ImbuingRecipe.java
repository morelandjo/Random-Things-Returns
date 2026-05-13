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

/**
 * Imbuing Station recipe: three unordered ingredient slots, one centre "to-imbue" slot,
 * and a fixed ItemStack output. Matching tolerates the three ingredient slots in any order
 * but requires the centre stack to test against {@link #toImbue}.
 */
public record ImbuingRecipe(
        Ingredient ingredient1,
        Ingredient ingredient2,
        Ingredient ingredient3,
        Ingredient toImbue,
        ItemStack result
) implements Recipe<ImbuingRecipe.Input> {

    public static final int CRAFT_TICKS = 200;
    public static final int SLOT_INGREDIENT_1 = 0;
    public static final int SLOT_INGREDIENT_2 = 1;
    public static final int SLOT_INGREDIENT_3 = 2;
    public static final int SLOT_CENTRE = 3;
    public static final int SLOT_OUTPUT = 4;

    public static final MapCodec<ImbuingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Ingredient.CODEC.fieldOf("ingredient1").forGetter(ImbuingRecipe::ingredient1),
            Ingredient.CODEC.fieldOf("ingredient2").forGetter(ImbuingRecipe::ingredient2),
            Ingredient.CODEC.fieldOf("ingredient3").forGetter(ImbuingRecipe::ingredient3),
            Ingredient.CODEC.fieldOf("center").forGetter(ImbuingRecipe::toImbue),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ImbuingRecipe::result)
        ).apply(instance, ImbuingRecipe::new)
    );

    public record Input(ItemStack i1, ItemStack i2, ItemStack i3, ItemStack centre) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return switch (slot) {
                case SLOT_INGREDIENT_1 -> i1;
                case SLOT_INGREDIENT_2 -> i2;
                case SLOT_INGREDIENT_3 -> i3;
                case SLOT_CENTRE -> centre;
                default -> ItemStack.EMPTY;
            };
        }

        @Override
        public int size() {
            return 4;
        }
    }

    @Override
    public boolean matches(Input input, Level level) {
        if (!toImbue.test(input.centre())) return false;

        ItemStack[] provided = { input.i1(), input.i2(), input.i3() };
        Ingredient[] needed = { ingredient1, ingredient2, ingredient3 };
        boolean[] consumed = new boolean[3];

        for (Ingredient ing : needed) {
            boolean matched = false;
            for (int i = 0; i < 3; i++) {
                if (consumed[i]) continue;
                if (ing.test(provided[i])) {
                    consumed[i] = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        // Reject extra non-empty ingredient stacks that nothing matched.
        for (int i = 0; i < 3; i++) {
            if (!consumed[i] && !provided[i].isEmpty()) return false;
        }
        return true;
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
        list.add(this.ingredient1);
        list.add(this.ingredient2);
        list.add(this.ingredient3);
        list.add(this.toImbue);
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.IMBUING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.IMBUING.get();
    }
}
