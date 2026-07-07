package lumien.randomthings.recipe;

import lumien.randomthings.item.EmeraldCompassItem;
import lumien.randomthings.item.IdCardItem;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.UUID;

/** Emerald Compass + bound ID Card → compass tracking that player (the card is kept). */
public class EmeraldCompassRecipe extends CustomRecipe {

    public EmeraldCompassRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        boolean hasCompass = false;
        boolean hasIdCard = false;
        int itemCount = 0;
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.EMERALD_COMPASS.get()) {
                    hasCompass = true;
                } else if (stack.getItem() == ModItems.ID_CARD.get() && IdCardItem.hasPlayerData(stack)) {
                    hasIdCard = true;
                } else {
                    return false;
                }
            }
        }
        return hasCompass && hasIdCard && itemCount == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registryAccess) {
        ItemStack compassStack = ItemStack.EMPTY;
        ItemStack idCardStack = ItemStack.EMPTY;
        for (int i = 0; i < input.getContainerSize(); i++) {
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
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < input.getContainerSize(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.getItem() == ModItems.ID_CARD.get()) {
                remaining.set(i, stack.copy());
            }
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.EMERALD_COMPASS.get();
    }
}
