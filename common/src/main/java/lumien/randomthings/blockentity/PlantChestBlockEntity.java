package lumien.randomthings.blockentity;

import lumien.randomthings.block.PlantChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PlantChestBlockEntity extends ChestBlockEntity {

    public PlantChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.PLANT_CHEST.get(), pos, state);

        this.setLootTable(PlantChestBlock.PLANT_CHEST_LOOT_TABLE, 0L);
    }
}
