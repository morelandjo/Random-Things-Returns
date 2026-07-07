package lumien.randomthings.block;

import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.blockentity.NatureCoreBlockEntity;
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
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

/** Spreads life around itself: sand→grass, bonemeal pulses, animal spawns, tree planting. */
public class BlockNatureCore extends BaseEntityBlock {

    public BlockNatureCore() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(25.0F, 2000.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD)
                .lightLevel(state -> 8));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NatureCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null
            : createTickerHelper(blockEntityType, ModBlockEntityTypes.NATURE_CORE.get(), NatureCoreBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
