package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Spectre Block - The indestructible, translucent walls of the Spectre Dimension rooms.
 * These blocks form the boundaries of player's private rooms and cannot be broken or destroyed.
 */
public class SpectreBlockBlock extends Block {

    public SpectreBlockBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(-1.0F, 3600000.0F) // Unbreakable
                .sound(SoundType.GLASS)
                .noOcclusion() // Allow translucent rendering
                .isViewBlocking((state, level, pos) -> false)
        );
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        // Cannot be destroyed by any entity
        return false;
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        // Cannot be destroyed by explosions
        return false;
    }

    @Override
    public float getExplosionResistance() {
        // Maximum explosion resistance
        return Float.MAX_VALUE - 1000f;
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        // Don't render face if adjacent block is also a spectre block or spectre core
        Block adjacentBlock = adjacentState.getBlock();
        return adjacentBlock == this || adjacentBlock == ModBlocks.SPECTRE_CORE.get();
    }
}
