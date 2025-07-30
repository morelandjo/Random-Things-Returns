package lumien.randomthings.datagen;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ModConstants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Simple blocks that use existing models/textures
        simpleBlockWithItem(ModBlocks.FERTILIZED_DIRT.get(), 
            models().cubeAll("fertilized_dirt", blockTexture(ModBlocks.FERTILIZED_DIRT.get())));
        
        simpleBlockWithItem(ModBlocks.SUPER_LUBRICENT_STONE.get(),
            models().cubeAll("super_lubricent_stone", blockTexture(ModBlocks.SUPER_LUBRICENT_STONE.get())));

        simpleBlockWithItem(ModBlocks.BLOCK_OF_STICKS.get(),
            models().cubeAll("block_of_sticks", blockTexture(ModBlocks.BLOCK_OF_STICKS.get())));

        simpleBlockWithItem(ModBlocks.BLOCK_OF_STICKS_RETURNING.get(),
            models().cubeAll("block_of_sticks_returning", blockTexture(ModBlocks.BLOCK_OF_STICKS_RETURNING.get())));

        // Platform blocks - use vanilla plank textures
        simpleBlockWithItem(ModBlocks.PLATFORM_OAK.get(),
            models().slab("platform_oak", mcLoc("block/oak_planks"), 
                mcLoc("block/oak_planks"), mcLoc("block/oak_planks")));

        simpleBlockWithItem(ModBlocks.PLATFORM_SPRUCE.get(),
            models().slab("platform_spruce", mcLoc("block/spruce_planks"), 
                mcLoc("block/spruce_planks"), mcLoc("block/spruce_planks")));

        simpleBlockWithItem(ModBlocks.PLATFORM_BIRCH.get(),
            models().slab("platform_birch", mcLoc("block/birch_planks"), 
                mcLoc("block/birch_planks"), mcLoc("block/birch_planks")));

        simpleBlockWithItem(ModBlocks.PLATFORM_JUNGLE.get(),
            models().slab("platform_jungle", mcLoc("block/jungle_planks"), 
                mcLoc("block/jungle_planks"), mcLoc("block/jungle_planks")));

        simpleBlockWithItem(ModBlocks.PLATFORM_ACACIA.get(),
            models().slab("platform_acacia", mcLoc("block/acacia_planks"), 
                mcLoc("block/acacia_planks"), mcLoc("block/acacia_planks")));

        simpleBlockWithItem(ModBlocks.PLATFORM_DARKOAK.get(),
            models().slab("platform_darkoak", mcLoc("block/dark_oak_planks"), 
                mcLoc("block/dark_oak_planks"), mcLoc("block/dark_oak_planks")));

        // Complex blocks that already have blockstates will be skipped
        // These include: Advanced Redstone Torch, Blood Rose, Rainbow Lamp
        // They use the existing JSON files which are more complex
    }

    private void simpleBlockWithItem(Block block, net.neoforged.neoforge.client.model.generators.BlockModelBuilder model) {
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }
}