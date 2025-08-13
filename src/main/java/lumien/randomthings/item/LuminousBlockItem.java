package lumien.randomthings.item;

import lumien.randomthings.block.LuminousBlock;
import lumien.randomthings.block.TranslucentLuminousBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LuminousBlockItem extends BlockItem {
    
    private final DyeColor color;
    
    public LuminousBlockItem(Block block, Properties properties, DyeColor color) {
        super(block, properties);
        this.color = color;
    }
    
    @Override
    protected BlockState getPlacementState(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            if (state.getBlock() instanceof LuminousBlock) {
                return state.setValue(LuminousBlock.COLOR, this.color);
            } else if (state.getBlock() instanceof TranslucentLuminousBlock) {
                return state.setValue(TranslucentLuminousBlock.COLOR, this.color);
            }
        }
        return state;
    }
    
    public DyeColor getColor() {
        return this.color;
    }
}