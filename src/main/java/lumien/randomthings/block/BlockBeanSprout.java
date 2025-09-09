package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.serialization.MapCodec;

import java.util.List;

public class BlockBeanSprout extends BushBlock implements BonemealableBlock {
    
    public static final MapCodec<BlockBeanSprout> CODEC = simpleCodec((properties) -> new BlockBeanSprout());
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
        Block.box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D),
        Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D),
        Block.box(5.0D, 0.0D, 5.0D, 11.0D, 7.0D, 11.0D),
        Block.box(5.0D, 0.0D, 5.0D, 11.0D, 8.0D, 11.0D),
        Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D),
        Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D),
        Block.box(4.0D, 0.0D, 4.0D, 12.0D, 11.0D, 12.0D),
        Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D)
    };
    
    public BlockBeanSprout() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<BlockBeanSprout> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(net.minecraft.tags.BlockTags.DIRT) || 
               state.getBlock() == net.minecraft.world.level.block.Blocks.GRASS_BLOCK ||
               state.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        return this.mayPlaceOn(level.getBlockState(blockpos), level, blockpos) && 
               level.getRawBrightness(pos, 0) >= 8;
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);
        
        if (age == 7) {
            if (!level.isClientSide) {
                // Harvest the beans
                level.setBlock(pos, state.setValue(AGE, 0), 3);
                
                RandomSource random = level.getRandom();
                int beans = random.nextInt(2) + 1; // 1-2 beans
                
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
                
                if (random.nextInt((int)(25.0F / growthSpeed) + 1) == 0) {
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
                
                if (soilState.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND) {
                    fertility = 1.0F;
                    if (soilState.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE) > 0) {
                        fertility = 3.0F;
                    }
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
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 7;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int currentAge = state.getValue(AGE);
        int newAge = Math.min(7, currentAge + random.nextInt(2, 6));
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }
}