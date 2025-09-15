package lumien.randomthings.recipe;

import lumien.randomthings.item.EmeraldCompassItem;
import lumien.randomthings.item.IdCardItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EmeraldCompassRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public EmeraldCompassRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasCompass = false;
        boolean hasIdCard = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.EMERALD_COMPASS.get()) {
                    hasCompass = true;
                } else if (stack.getItem() == ModItems.ID_CARD.get() && IdCardItem.hasPlayerData(stack)) {
                    hasIdCard = true;
                } else {
                    return false; // Invalid item
                }
            }
        }

        return hasCompass && hasIdCard && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack compassStack = ItemStack.EMPTY;
        ItemStack idCardStack = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.EMERALD_COMPASS.get()) {
                    compassStack = stack;
                } else if (stack.getItem() == ModItems.ID_CARD.get()) {
                    idCardStack = stack;
                }
            }
        }

        if (compassStack.isEmpty() || idCardStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create result compass with target player
        ItemStack result = compassStack.copy();
        
        UUID playerUUID = IdCardItem.getPlayerUUID(idCardStack);
        if (playerUUID != null) {
            EmeraldCompassItem.setTarget(result, playerUUID);
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.EMERALD_COMPASS.get());
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EMERALD_COMPASS.get();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.REDSTONE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(ModItems.EMERALD_COMPASS.get()));
        ingredients.add(Ingredient.of(ModItems.ID_CARD.get()));
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() == ModItems.ID_CARD.get()) {
                // Return the ID card as it should be reusable
                remainingItems.set(i, stack.copy());
            }
        }

        return remainingItems;
    }
}