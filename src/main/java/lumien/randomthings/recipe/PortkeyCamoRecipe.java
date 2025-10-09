package lumien.randomthings.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Custom recipe for applying camo to a portkey.
 * Accepts a portkey and any other item, and returns the portkey with the camo item's appearance.
 */
public class PortkeyCamoRecipe extends CustomRecipe {

    public PortkeyCamoRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasPortkey = false;
        boolean hasOtherItem = false;
        int itemCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                if (stack.getItem() == ModItems.PORTKEY.get()) {
                    hasPortkey = true;
                } else {
                    hasOtherItem = true;
                }
            }
        }

        // Must have exactly 2 items: portkey + one other item
        return itemCount == 2 && hasPortkey && hasOtherItem;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack portkey = ItemStack.EMPTY;
        ItemStack camoItem = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() == ModItems.PORTKEY.get()) {
                    portkey = stack;
                } else {
                    camoItem = stack;
                }
            }
        }

        if (portkey.isEmpty() || camoItem.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create a copy of the portkey
        ItemStack result = portkey.copy();

        // Store the camo item's ResourceLocation
        net.minecraft.resources.ResourceLocation camoItemId =
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(camoItem.getItem());
        result.set(ModDataComponents.PORTKEY_CAMO.get(), camoItemId);

        // Debug logging
        System.out.println("PortkeyCamoRecipe: Setting camo to " + camoItemId);
        System.out.println("PortkeyCamoRecipe: Result has camo: " + result.get(ModDataComponents.PORTKEY_CAMO.get()));

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PORTKEY_CAMO.get();
    }

    public static class Serializer implements RecipeSerializer<PortkeyCamoRecipe> {
        private static final MapCodec<PortkeyCamoRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category())
            ).apply(instance, PortkeyCamoRecipe::new)
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, PortkeyCamoRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> buf.writeEnum(recipe.category()),
            buf -> new PortkeyCamoRecipe(buf.readEnum(CraftingBookCategory.class))
        );

        @Override
        public MapCodec<PortkeyCamoRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PortkeyCamoRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
