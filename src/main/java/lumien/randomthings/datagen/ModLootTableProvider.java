package lumien.randomthings.datagen;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    public static class ModBlockLootTables extends BlockLootSubProvider {
        public ModBlockLootTables(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            // Simple drops
            dropSelf(ModBlocks.ADVANCED_REDSTONE_TORCH.get());
            // Wall torch drops the standing torch
            add(ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get(), createSingleItemTable(ModBlocks.ADVANCED_REDSTONE_TORCH.get()));
            dropSelf(ModBlocks.FERTILIZED_DIRT.get());
            dropSelf(ModBlocks.RAINBOW_LAMP.get());
            dropSelf(ModBlocks.SUPER_LUBRICENT_STONE.get());
            dropSelf(ModBlocks.PLATFORM_OAK.get());
            dropSelf(ModBlocks.PLATFORM_SPRUCE.get());
            dropSelf(ModBlocks.PLATFORM_BIRCH.get());
            dropSelf(ModBlocks.PLATFORM_JUNGLE.get());
            dropSelf(ModBlocks.PLATFORM_ACACIA.get());
            dropSelf(ModBlocks.PLATFORM_DARKOAK.get());

            // Blood Rose drops both the block and petals
            add(ModBlocks.BLOOD_ROSE.get(), 
                createSingleItemTableWithSilkTouch(ModBlocks.BLOOD_ROSE.get(), ModItems.BLOOD_ROSE_PETAL.get(), 
                    net.minecraft.world.level.storage.loot.providers.number.UniformGenerator.between(1, 3)));

            // Block of Sticks variants
            dropSelf(ModBlocks.BLOCK_OF_STICKS.get());
            dropSelf(ModBlocks.BLOCK_OF_STICKS_RETURNING.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).collect(java.util.stream.Collectors.toList());
        }
    }
}