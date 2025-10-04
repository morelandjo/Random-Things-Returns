package lumien.randomthings.recipe;

import lumien.randomthings.item.GoldenCompassItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class GoldenCompassRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public GoldenCompassRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasCompass = false;
        boolean hasFilter = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.GOLDEN_COMPASS.get()) {
                    hasCompass = true;
                } else if (stack.getItem() == ModItems.POSITION_FILTER.get() && PositionFilterItem.hasPosition(stack)) {
                    hasFilter = true;
                } else {
                    return false; // Invalid item
                }
            }
        }

        return hasCompass && hasFilter && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack compassStack = ItemStack.EMPTY;
        ItemStack filterStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.GOLDEN_COMPASS.get()) {
                    compassStack = stack;
                } else if (stack.getItem() == ModItems.POSITION_FILTER.get()) {
                    filterStack = stack;
                }
            }
        }

        if (compassStack.isEmpty() || filterStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create result compass with target position
        ItemStack result = compassStack.copy();

        BlockPos pos = PositionFilterItem.getPosition(filterStack);
        if (pos != null) {
            GoldenCompassItem.setTarget(result, pos.getX(), pos.getZ());
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.GOLDEN_COMPASS.get());
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GOLDEN_COMPASS.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(ModItems.GOLDEN_COMPASS.get()));
        ingredients.add(Ingredient.of(ModItems.POSITION_FILTER.get()));
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() == ModItems.POSITION_FILTER.get()) {
                // Return the position filter as it should be reusable
                remainingItems.set(i, stack.copy());
            }
        }

        return remainingItems;
    }
}
