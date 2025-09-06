package lumien.randomthings.item;

import lumien.randomthings.block.StainedBrickBlock;
import lumien.randomthings.block.LuminousStainedBrickBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StainedBrickItem extends BlockItem {
    
    private final DyeColor color;
    
    public StainedBrickItem(Block block, Properties properties, DyeColor color) {
        super(block, properties);
        this.color = color;
    }
    
    @Override
    protected BlockState getPlacementState(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            if (state.getBlock() instanceof StainedBrickBlock) {
                return state.setValue(StainedBrickBlock.COLOR, this.color);
            } else if (state.getBlock() instanceof LuminousStainedBrickBlock) {
                return state.setValue(LuminousStainedBrickBlock.COLOR, this.color);
            }
        }
        return state;
    }
    
    public DyeColor getColor() {
        return this.color;
    }
}