package lumien.randomthings.block;

import lumien.randomthings.blockentity.AncientFurnaceBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class AncientFurnaceBlock extends BaseEntityBlock {

    public AncientFurnaceBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F) // Unbreakable
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .noOcclusion());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientFurnaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ?
            createTickerHelper(blockEntityType, ModBlockEntityTypes.ANCIENT_FURNACE.get(), AncientFurnaceBlockEntity::clientTick) :
            createTickerHelper(blockEntityType, ModBlockEntityTypes.ANCIENT_FURNACE.get(), AncientFurnaceBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
