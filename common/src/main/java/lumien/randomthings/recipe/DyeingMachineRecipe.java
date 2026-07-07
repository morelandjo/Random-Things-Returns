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
 * Dyeing Machine recipe (1.20.1 pre-codec recipe API): an input item + a dye → a result item.
 * Matched against the machine's inventory (slot 0 = input, slot 1 = dye).
 */
public record DyeingMachineRecipe(ResourceLocation id, Ingredient input, Ingredient dye, ItemStack result)
        implements Recipe<Container> {

    @Override
    public boolean matches(Container container, Level level) {
        return this.input.test(container.getItem(0)) && this.dye.test(container.getItem(1));
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
        list.add(this.input);
        list.add(this.dye);
        return list;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DYEING_MACHINE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.DYEING_MACHINE.get();
    }
}
