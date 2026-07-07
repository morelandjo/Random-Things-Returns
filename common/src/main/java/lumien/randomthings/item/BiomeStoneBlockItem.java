package lumien.randomthings.item;

import lumien.randomthings.block.BiomeStoneBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** BlockItem that places Biome Stone with a chosen carved variant. */
public class BiomeStoneBlockItem extends BlockItem {

    private final BiomeStoneBlock.Variant variant;

    public BiomeStoneBlockItem(Block block, Properties properties, BiomeStoneBlock.Variant variant) {
        super(block, properties);
        this.variant = variant;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            return state.setValue(BiomeStoneBlock.VARIANT, this.variant);
        }
        return null;
    }

    public BiomeStoneBlock.Variant getVariant() {
        return this.variant;
    }
}
