package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Bean sprout crop; grows through 8 stages, harvest the mature plant for beans. */
public class BlockBeanSprout extends BushBlock implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(5, 0, 5, 11, 5, 11), Block.box(5, 0, 5, 11, 6, 11),
        Block.box(5, 0, 5, 11, 7, 11), Block.box(5, 0, 5, 11, 8, 11),
        Block.box(4, 0, 4, 12, 9, 12), Block.box(4, 0, 4, 12, 10, 12),
        Block.box(4, 0, 4, 12, 11, 12), Block.box(4, 0, 4, 12, 12, 12)
    };

    public BlockBeanSprout() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak()
            .sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below) && level.getRawBrightness(pos, 0) >= 8;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) == 7) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(AGE, 0), 3);
                int beans = level.getRandom().nextInt(2) + 1;
                ItemStack beanStack = new ItemStack(ModItems.BEAN.get(), beans);
                if (!player.getInventory().add(beanStack)) {
                    player.drop(beanStack, false);
                }
                level.playSound(null, pos, SoundType.CROP.getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = state.getValue(AGE);
            if (age < 7) {
                float growthSpeed = getGrowthSpeed(state, level, pos);
                if (random.nextInt((int) (25.0F / growthSpeed) + 1) == 0) {
                    level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                }
            }
        }
    }

    protected float getGrowthSpeed(BlockState state, BlockGetter level, BlockPos pos) {
        float speed = 1.0F;
        BlockPos belowPos = pos.below();
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                float fertility = 0.0F;
                BlockState soilState = level.getBlockState(belowPos.offset(x, 0, z));
                if (soilState.getBlock() == Blocks.FARMLAND) {
                    fertility = soilState.getValue(FarmBlock.MOISTURE) > 0 ? 3.0F : 1.0F;
                }
                if (x != 0 || z != 0) {
                    fertility /= 4.0F;
                }
                speed += fertility;
            }
        }
        return speed;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return state.getValue(AGE) < 7;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(7, state.getValue(AGE) + random.nextInt(2, 6));
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}
