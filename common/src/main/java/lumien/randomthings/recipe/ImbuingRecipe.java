package lumien.randomthings.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Imbuing Station recipe (1.20.1 pre-codec): three unordered ingredient slots, one centre
 * "to-imbue" slot, and a fixed result. Matched against the station inventory (slots 0-2 =
 * ingredients, slot 3 = centre).
 */
public record ImbuingRecipe(
        ResourceLocation id,
        Ingredient ingredient1,
        Ingredient ingredient2,
        Ingredient ingredient3,
        Ingredient toImbue,
        ItemStack result
) implements Recipe<Container> {

    public static final int CRAFT_TICKS = 200;
    public static final int SLOT_INGREDIENT_1 = 0;
    public static final int SLOT_INGREDIENT_2 = 1;
    public static final int SLOT_INGREDIENT_3 = 2;
    public static final int SLOT_CENTRE = 3;
    public static final int SLOT_OUTPUT = 4;

    @Override
    public boolean matches(Container container, Level level) {
        if (!toImbue.test(container.getItem(SLOT_CENTRE))) return false;

        ItemStack[] provided = {
            container.getItem(SLOT_INGREDIENT_1),
            container.getItem(SLOT_INGREDIENT_2),
            container.getItem(SLOT_INGREDIENT_3)
        };
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

        for (int i = 0; i < 3; i++) {
            if (!consumed[i] && !provided[i].isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
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
    public ResourceLocation getId() {
        return this.id;
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
