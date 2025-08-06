package lumien.randomthings.recipe;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.ModDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class DiaphanousBlockRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public DiaphanousBlockRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasDiaphanous = false;
        boolean hasBlock = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    hasDiaphanous = true;
                } else if (Block.byItem(stack.getItem()) != null) {
                    hasBlock = true;
                } else {
                    return false; // Invalid item
                }
            }
        }

        return hasDiaphanous && hasBlock && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack diaphanousStack = ItemStack.EMPTY;
        ItemStack blockStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    diaphanousStack = stack;
                } else {
                    blockStack = stack;
                }
            }
        }

        if (diaphanousStack.isEmpty() || blockStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create result with the block's texture
        ItemStack result = new ItemStack(ModItems.DIAPHANOUS_BLOCK.get());
        
        // Get the block from the item
        Block block = Block.byItem(blockStack.getItem());
        if (block != null) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
            result.set(ModDataComponents.DIAPHANOUS_BLOCK_STATE.get(), blockId);
        }
        
        // Preserve inverted state if it exists
        Boolean inverted = diaphanousStack.get(ModDataComponents.DIAPHANOUS_INVERTED.get());
        if (inverted != null) {
            result.set(ModDataComponents.DIAPHANOUS_INVERTED.get(), inverted);
        } else {
            result.set(ModDataComponents.DIAPHANOUS_INVERTED.get(), false);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.DIAPHANOUS_BLOCK.get());
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DIAPHANOUS_BLOCK.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.BUILDING;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(ModItems.DIAPHANOUS_BLOCK.get()));
        // Can't specify "any block" in ingredients, so this is just for display
        return ingredients;
    }
}