package lumien.randomthings.recipe;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Diaphanous Block + any block → diaphanous block disguised as that block (inverted flag preserved). */
public class DiaphanousBlockRecipe extends CustomRecipe {

    public DiaphanousBlockRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        boolean hasDiaphanous = false;
        boolean hasBlock = false;
        int itemCount = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    hasDiaphanous = true;
                } else if (Block.byItem(stack.getItem()) != Blocks.AIR) {
                    hasBlock = true;
                } else {
                    return false;
                }
            }
        }
        // A lone diaphanous block toggles its inverted flag; with a block it changes appearance.
        return hasDiaphanous && (itemCount == 1 || (hasBlock && itemCount == 2));
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registryAccess) {
        ItemStack diaphanousStack = ItemStack.EMPTY;
        ItemStack blockStack = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    diaphanousStack = stack;
                } else {
                    blockStack = stack;
                }
            }
        }
        if (diaphanousStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(ModItems.DIAPHANOUS_BLOCK.get());
        if (blockStack.isEmpty()) {
            // Lone diaphanous block: toggle inverted, keep appearance.
            net.minecraft.resources.ResourceLocation current =
                RTNbt.getResourceLocation(diaphanousStack, RTDataKeys.DIAPHANOUS_BLOCK_STATE);
            if (current != null) {
                RTNbt.setResourceLocation(result, RTDataKeys.DIAPHANOUS_BLOCK_STATE, current);
            }
            RTNbt.setBoolean(result, RTDataKeys.DIAPHANOUS_INVERTED, !RTNbt.getBoolean(diaphanousStack, RTDataKeys.DIAPHANOUS_INVERTED));
            return result;
        }
        Block block = Block.byItem(blockStack.getItem());
        if (block != Blocks.AIR) {
            RTNbt.setResourceLocation(result, RTDataKeys.DIAPHANOUS_BLOCK_STATE, BuiltInRegistries.BLOCK.getKey(block));
        }
        RTNbt.setBoolean(result, RTDataKeys.DIAPHANOUS_INVERTED, RTNbt.getBoolean(diaphanousStack, RTDataKeys.DIAPHANOUS_INVERTED));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        return NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DIAPHANOUS_BLOCK.get();
    }
}
