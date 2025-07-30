package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class RainbowLampBlock extends Block {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public RainbowLampBlock() {
        super(BlockBehaviour.Properties.of()
            .lightLevel((state) -> 15)
            .strength(0.3F)
            .sound(SoundType.GLASS));

        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLOR);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            updateColor(state, level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!level.isClientSide) {
            updateColor(state, level, pos);
        }
    }

    private void updateColor(BlockState state, Level level, BlockPos pos) {
        int redstoneLevel = level.getBestNeighborSignal(pos);

        int colorIndex = state.getValue(COLOR).ordinal();

        if (redstoneLevel != colorIndex) {
            DyeColor[] colors = DyeColor.values();
            if (redstoneLevel < colors.length) {
                level.setBlock(pos, state.setValue(COLOR, colors[redstoneLevel]), 2);
            }
        }
    }
}