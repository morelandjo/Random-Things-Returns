package lumien.randomthings.block;

import lumien.randomthings.blockentity.FlooBrickBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class FlooBrickBlock extends BaseEntityBlock {
    public static final MapCodec<FlooBrickBlock> CODEC = simpleCodec(properties -> new FlooBrickBlock());

    public FlooBrickBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_RED)
            .strength(2.0F, 10.0F)
            .sound(SoundType.STONE)
            .noLootTable());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlooBrickBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
