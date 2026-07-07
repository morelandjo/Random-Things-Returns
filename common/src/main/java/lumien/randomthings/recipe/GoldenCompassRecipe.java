package lumien.randomthings.recipe;

import lumien.randomthings.item.GoldenCompassItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Golden Compass + Position Filter → compass pointing at that spot (the filter is kept). */
public class GoldenCompassRecipe extends CustomRecipe {

    public GoldenCompassRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        boolean hasCompass = false;
        boolean hasFilter = false;
        int itemCount = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.GOLDEN_COMPASS.get()) {
                    hasCompass = true;
                } else if (stack.getItem() == ModItems.POSITION_FILTER.get() && PositionFilterItem.getPosition(stack) != null) {
                    hasFilter = true;
                } else {
                    return false;
                }
            }
        }
        return hasCompass && hasFilter && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registryAccess) {
        ItemStack compassStack = ItemStack.EMPTY;
        ItemStack filterStack = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
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
        BlockPos pos = PositionFilterItem.getPosition(filterStack);
        if (pos == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = compassStack.copy();
        GoldenCompassItem.setTarget(result, pos.getX(), pos.getZ());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() == ModItems.POSITION_FILTER.get()) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GOLDEN_COMPASS.get();
    }
}
