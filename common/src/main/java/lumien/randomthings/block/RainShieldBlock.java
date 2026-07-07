package lumien.randomthings.block;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RainShieldBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 1.0D, 10.0D), // Base
        Block.box(7.0D, 1.0D, 7.0D, 9.0D, 16.0D, 9.0D)   // Pole
    );

    public RainShieldBlock() {
        super(Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.0F)
            .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RainShieldBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.getBlockEntity(pos) instanceof RainShieldBlockEntity rainShield) {
            rainShield.onBlockAdded(level, pos, state);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);

        if (level.getBlockEntity(pos) instanceof RainShieldBlockEntity rainShield) {
            rainShield.neighborChanged(state, level, pos, neighborBlock, neighborPos);
        }

        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    protected boolean canSurvive(BlockState state, Level level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        // Flame/smoke spiral while raining and active
        if (level.isRaining()
            && level.getBlockEntity(pos) instanceof RainShieldBlockEntity rainShield && rainShield.isActive()) {
            for (double mod = 0; mod < 1; mod += 0.1f) {
                for (double a = 0; a <= Math.PI * 2D; a += (Math.PI * 2D) / 3D) {
                    double x = pos.getX() + 0.5 + (1 - mod) * Math.cos(a);
                    double z = pos.getZ() + 0.5 + (1 - mod) * Math.sin(a);

                    level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.7f + mod, z, 0, 0, 0);
                    level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 0.6f + mod, z, 0, 0, 0);
                }
            }
        }
    }
}
