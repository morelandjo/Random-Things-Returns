package lumien.randomthings.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Custom recipe for combining a Spectre Anchor with any non-stackable item
 * to make that item persist through death.
 */
public class SpectreAnchorRecipe implements CraftingRecipe {
    private final ResourceLocation id;

    public SpectreAnchorRecipe(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack anchor = null;
        ItemStack target = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty()) {
                if (stack.is(ModItems.SPECTRE_ANCHOR.get())) {
                    if (anchor == null) {
                        anchor = stack;
                    } else {
                        // Multiple anchors - invalid
                        return false;
                    }
                } else {
                    if (target == null) {
                        // Check if item is non-stackable
                        if (stack.getMaxStackSize() != 1) {
                            return false;
                        }
                        // Check if already anchored
                        if (stack.has(ModDataComponents.SPECTRE_ANCHORED.get())) {
                            return false;
                        }
                        target = stack;
                    } else {
                        // Multiple items - invalid
                        return false;
                    }
                }
            }
        }

        // Must have exactly one anchor and one non-anchored non-stackable item
        return anchor != null && target != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack target = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty() && !stack.is(ModItems.SPECTRE_ANCHOR.get())) {
                target = stack;
                break;
            }
        }

        if (target == null) {
            return ItemStack.EMPTY;
        }

        // Create a copy of the target item and mark it as anchored
        ItemStack result = target.copy();
        result.set(ModDataComponents.SPECTRE_ANCHORED.get(), true);

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Can be crafted in any crafting grid
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        // Return a Spectre Anchor as the generic result for JEI
        return new ItemStack(ModItems.SPECTRE_ANCHOR.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SPECTRE_ANCHOR.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeType.CRAFTING;
    }

    @Override
    public boolean isSpecial() {
        // Mark as special so it doesn't show in the recipe book
        return true;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    public ResourceLocation getId() {
        return id;
    }
}
