package lumien.randomthings.item;

import lumien.randomthings.block.ColoredGrassBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class ColoredGrassItem extends BlockItem {
    private final DyeColor color;

    public ColoredGrassItem(Block block, DyeColor color, Properties properties) {
        super(block, properties);
        this.color = color;
    }

    public DyeColor getColor() {
        return color;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) return null;
        return state.setValue(ColoredGrassBlock.COLOR, color);
    }
}
