package lumien.randomthings.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Spectre Block — the indestructible, translucent walls of the Spectre Dimension rooms.
 * Unbreakability comes from the very high strength/resistance (the Forge-only
 * {@code canEntityDestroy}/{@code canDropFromExplosion} hooks are not available cross-loader).
 */
public class SpectreBlockBlock extends Block {

    public SpectreBlockBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .strength(-1.0F, 3600000.0F) // unbreakable + blast-proof
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.getBlock() == this;
    }
}
