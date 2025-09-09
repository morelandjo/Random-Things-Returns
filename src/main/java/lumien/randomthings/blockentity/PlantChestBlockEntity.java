package lumien.randomthings.blockentity;

import lumien.randomthings.block.PlantChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public class PlantChestBlockEntity extends ChestBlockEntity {
    
    public PlantChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PLANT_CHEST.get(), pos, state);
        
        // Set the loot table for plant chest
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, PlantChestBlock.PLANT_CHEST_LOOT_TABLE);
        this.setLootTable(lootTableKey, 0L);
    }
}