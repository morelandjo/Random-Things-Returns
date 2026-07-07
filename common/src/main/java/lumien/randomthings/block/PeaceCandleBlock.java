package lumien.randomthings.block;

import lumien.randomthings.blockentity.PeaceCandleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A candle that keeps monsters from spawning in the surrounding chunks. */
public class PeaceCandleBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Shapes.or(
        Block.box(6.0, 0.0, 6.0, 10.0, 1.0, 10.0),
        Block.box(7.5, 1.0, 7.5, 8.5, 4.0, 8.5),
        Block.box(5.0, 1.0, 6.0, 6.0, 2.0, 10.0),
        Block.box(10.0, 1.0, 6.0, 11.0, 2.0, 10.0),
        Block.box(6.0, 1.0, 5.0, 10.0, 2.0, 6.0),
        Block.box(6.0, 1.0, 10.0, 10.0, 2.0, 11.0));

    public PeaceCandleBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(2.0F).sound(SoundType.STONE).lightLevel(state -> 14).noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PeaceCandleBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.3D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.FLAME,
            x + (random.nextDouble() - 0.5D) * 0.1D, y + random.nextDouble() * 0.1D, z + (random.nextDouble() - 0.5D) * 0.1D, 0, 0, 0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                x + (random.nextDouble() - 0.5D) * 0.1D, y + random.nextDouble() * 0.1D, z + (random.nextDouble() - 0.5D) * 0.1D, 0, 0.01D, 0);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!this.canSurvive(state, level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }
}
