package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

public class LotusBlock extends BushBlock {
    public static final MapCodec<LotusBlock> CODEC = simpleCodec(properties -> new LotusBlock());
    
    @Override
    public MapCodec<LotusBlock> codec() {
        return CODEC;
    }
    
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(0.0625D * 16, 0.0D, 0.0625D * 16, 0.9375D * 16, 0.3125D * 16, 0.9375D * 16),
        Block.box(0.0625D * 16, 0.0D, 0.0625D * 16, 0.9375D * 16, 0.5625D * 16, 0.9375D * 16),
        Block.box(0.0625D * 16, 0.0D, 0.0625D * 16, 0.9375D * 16, 0.6875D * 16, 0.9375D * 16),
        Block.box(0.0625D * 16, 0.0D, 0.0625D * 16, 0.9375D * 16, 0.875D * 16, 0.9375D * 16)
    };

    public LotusBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
        
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return this.mayPlaceOn(level.getBlockState(belowPos), level, belowPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        
        if (age < 3) {
            if (random.nextInt(10) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (state.getValue(AGE) == 3) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(AGE, 0), 2);
                popResource(level, pos, new ItemStack(ModItems.LOTUS_BLOSSOM.get()));
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }


    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= 3;
    }

    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }

    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(AGE, Integer.valueOf(age));
    }
}