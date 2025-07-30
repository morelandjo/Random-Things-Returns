package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

public class FertilizedDirtBlock extends Block {
    private static final VoxelShape SHAPE_TILLED = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public static final BooleanProperty TILLED = BooleanProperty.create("tilled");

    public FertilizedDirtBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.5F)
            .sound(SoundType.GRAVEL)
            .randomTicks());

        this.registerDefaultState(this.stateDefinition.any().setValue(TILLED, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        for (int i = 0; i < 3; i++) {
            BlockState aboveState = level.getBlockState(pos.above());
            
            if (aboveState.isRandomlyTicking()) {
                aboveState.randomTick(level, pos.above(), random);
            } else {
                break;
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(TILLED) ? SHAPE_TILLED : Shapes.block();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILLED);
    }

    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, BlockState plantState) {
        boolean tilled = state.getValue(TILLED);
        Block plantBlock = plantState.getBlock();

        if (plantBlock instanceof StemBlock) {
            return TriState.TRUE;
        }

        if (plantState.is(net.minecraft.tags.BlockTags.CROPS)) {
            return tilled ? TriState.TRUE : TriState.FALSE;
        }

        if (plantState.is(net.minecraft.tags.BlockTags.FLOWERS) || 
            plantState.is(net.minecraft.tags.BlockTags.SAPLINGS) ||
            plantState.is(Blocks.SHORT_GRASS) ||
            plantState.is(Blocks.FERN)) {
            return !tilled ? TriState.TRUE : TriState.FALSE;
        }

        return TriState.DEFAULT;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}