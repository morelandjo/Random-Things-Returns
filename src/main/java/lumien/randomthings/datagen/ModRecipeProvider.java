package lumien.randomthings.datagen;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Advanced Redstone Torch - using original recipe design
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.ADVANCED_REDSTONE_TORCH.get())
                .pattern(" X ")
                .pattern("X#X")
                .pattern(" S ")
                .define('X', Items.REDSTONE)
                .define('#', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(recipeOutput);

        // Block of Sticks - using original recipe design  
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.BLOCK_OF_STICKS.get(), 16)
                .pattern("SSS")
                .pattern("S S")
                .pattern("SSS")
                .define('S', Items.STICK)
                .unlockedBy("has_sticks", has(Items.STICK))
                .save(recipeOutput);

        // Block of Sticks Returning
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.BLOCK_OF_STICKS_RETURNING.get())
                .pattern("E")
                .pattern("B")
                .define('E', Items.ENDER_PEARL)
                .define('B', ModItems.BLOCK_OF_STICKS.get())
                .unlockedBy("has_block_of_sticks", has(ModItems.BLOCK_OF_STICKS.get()))
                .save(recipeOutput);

        // Fertilized Dirt - using original recipe design
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FERTILIZED_DIRT.get(), 2)
                .pattern("RBR")
                .pattern("BDB")
                .pattern("RBR")
                .define('R', Items.ROTTEN_FLESH)
                .define('B', Items.BONE_MEAL)
                .define('D', Blocks.DIRT)
                .unlockedBy("has_dirt", has(Blocks.DIRT))
                .save(recipeOutput);

        // Platform blocks - using original recipe design
        for (var platformBlock : new Object[][]{
                {ModItems.PLATFORM_OAK.get(), Blocks.OAK_PLANKS},
                {ModItems.PLATFORM_SPRUCE.get(), Blocks.SPRUCE_PLANKS},
                {ModItems.PLATFORM_BIRCH.get(), Blocks.BIRCH_PLANKS},
                {ModItems.PLATFORM_JUNGLE.get(), Blocks.JUNGLE_PLANKS},
                {ModItems.PLATFORM_ACACIA.get(), Blocks.ACACIA_PLANKS},
                {ModItems.PLATFORM_DARKOAK.get(), Blocks.DARK_OAK_PLANKS}
        }) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, (net.minecraft.world.item.Item) platformBlock[0], 6)
                    .pattern("WWW")
                    .pattern(" E ")
                    .define('W', (net.minecraft.world.level.ItemLike) platformBlock[1])
                    .define('E', Items.ENDER_PEARL)
                    .unlockedBy("has_planks", has((net.minecraft.world.level.ItemLike) platformBlock[1]))
                    .save(recipeOutput);
        }

        // Rainbow Lamp - using original recipe design  
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.RAINBOW_LAMP.get())
                .pattern("   ")
                .pattern(" G ")
                .pattern("RLB")
                .define('G', Items.GREEN_DYE)
                .define('R', Items.RED_DYE)
                .define('B', Items.LAPIS_LAZULI)
                .define('L', Blocks.REDSTONE_LAMP)
                .unlockedBy("has_redstone_lamp", has(Blocks.REDSTONE_LAMP))
                .save(recipeOutput);

        // Super Lubricent Stone - using original recipe design
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SUPER_LUBRICENT_STONE.get(), 4)
                .pattern("GSG")
                .pattern("STS")
                .pattern("GSG")
                .define('G', Items.SNOWBALL)
                .define('S', Items.SEAGRASS)
                .define('T', Blocks.STONE)
                .unlockedBy("has_stone", has(Blocks.STONE))
                .save(recipeOutput);

        // Divining Rods
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_VANILLA.get(), Items.STICK);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_COAL.get(), Items.COAL);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_IRON.get(), Items.IRON_INGOT);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_GOLD.get(), Items.GOLD_INGOT);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_DIAMOND.get(), Items.DIAMOND);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_EMERALD.get(), Items.EMERALD);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_LAPIS.get(), Items.LAPIS_LAZULI);
        createDiviningRodRecipe(recipeOutput, ModItems.DIVINING_ROD_REDSTONE.get(), Items.REDSTONE);
    }

    private void createDiviningRodRecipe(RecipeOutput recipeOutput, net.minecraft.world.item.Item rod, net.minecraft.world.item.Item material) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, rod)
                .pattern("MSM")
                .pattern("SES")
                .pattern("S S")
                .define('M', material)
                .define('S', Items.STICK)
                .define('E', Items.SPIDER_EYE)
                .unlockedBy("has_" + material.toString().toLowerCase(), has(material))
                .save(recipeOutput);
    }
}