package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.EnderAnchorBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nullable;

public class EnderAnchorBlock extends BaseEntityBlock {

    public static final MapCodec<EnderAnchorBlock> CODEC = simpleCodec(properties -> new EnderAnchorBlock());

    public EnderAnchorBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5f)
                .lightLevel((state) -> 7));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderAnchorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.ENDER_ANCHOR.get(), EnderAnchorBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EnderAnchorBlockEntity enderAnchor) {
                enderAnchor.discardTicket();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}