package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockBeanStalk extends Block {
    
    public static final MapCodec<BlockBeanStalk> CODEC = simpleCodec((properties) -> new BlockBeanStalk());
    
    protected static final VoxelShape STALK_AABB = Block.box(6.4, 0, 6.4, 9.6, 16, 9.6);
    
    public BlockBeanStalk() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.4F)
                .sound(SoundType.LADDER)
                .noOcclusion()
                .randomTicks()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    }

    @Override
    public MapCodec<BlockBeanStalk> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return STALK_AABB;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.onGround() || entity.verticalCollision) {
            return;
        }

        double speed = 0.2; // Lesser bean stalk speed

        if (entity.getDeltaMovement().y >= 0.1) {
            BlockPos topPos = new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()) + 3, Mth.floor(entity.getZ()));
            Block topBlock = level.getBlockState(topPos).getBlock();
            if (topBlock == this) {
                entity.setPos(entity.getX(), entity.getY() + speed, entity.getZ());
            }
        } else if (entity.getDeltaMovement().y <= -0.1) {
            BlockPos bottomPos = new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()) - 3, Mth.floor(entity.getZ()));
            Block bottomBlock = level.getBlockState(bottomPos).getBlock();
            if (bottomBlock == Blocks.AIR || bottomBlock == this) {
                entity.setPos(entity.getX(), entity.getY() - speed, entity.getZ());
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            // Check if we can grow upward
            if (pos.getY() >= level.getMaxBuildHeight() || !level.isEmptyBlock(pos.above())) {
                return;
            }

            BlockState upState = level.getBlockState(pos.above());
            if (upState.getDestroySpeed(level, pos.above()) != -1.0F) {
                if (!level.isEmptyBlock(pos.above())) {
                    level.levelEvent(2001, pos.above(), Block.getId(upState));
                } else {
                    level.levelEvent(2001, pos, Block.getId(this.defaultBlockState()));
                    level.playSound(null, pos, this.getSoundType(state, level, pos, null).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 2.0F);
                }

                level.levelEvent(2005, pos.above(), 0);
                level.setBlock(pos.above(), this.defaultBlockState(), 3);
                level.scheduleTick(pos.above(), this, 5); // Slower growth than magic bean stalk
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 5);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        if (level.isEmptyBlock(belowPos)) {
            return false;
        }
        
        BlockState belowState = level.getBlockState(belowPos);
        Block belowBlock = belowState.getBlock();
        
        return belowBlock == Blocks.GRASS_BLOCK || 
               belowBlock == Blocks.DIRT || 
               belowBlock == Blocks.COARSE_DIRT ||
               belowBlock == Blocks.PODZOL ||
               belowBlock == Blocks.ROOTED_DIRT ||
               belowBlock instanceof BlockBeanStalk;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }
}