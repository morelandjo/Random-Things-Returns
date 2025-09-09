package lumien.randomthings.block;

import lumien.randomthings.block.ModBlocks;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockSpecialBeanStalk extends Block {
    
    public static final MapCodec<BlockSpecialBeanStalk> CODEC = simpleCodec((properties) -> new BlockSpecialBeanStalk());
    
    protected static final VoxelShape STALK_AABB = Block.box(6.4, 0, 6.4, 9.6, 16, 9.6);
    
    public BlockSpecialBeanStalk() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.4F)
                .sound(SoundType.LADDER)
                .noOcclusion()
                .randomTicks()
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY));
    }

    @Override
    public MapCodec<BlockSpecialBeanStalk> codec() {
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

        double speed = 0.5; // Magic bean stalk is faster than lesser

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
            // Magic bean stalk grows to world height and places bean pod at top
            int worldHeight = level.getMaxBuildHeight() - 1; // Safe height limit
            
            if (pos.getY() >= worldHeight - 1) {
                // Place bean pod at the top
                BlockPos podPos = pos.above();
                BlockState podReplaceState = level.getBlockState(podPos);
                
                if (podReplaceState.getDestroySpeed(level, podPos) != -1.0F || level.isEmptyBlock(podPos)) {
                    level.setBlock(podPos, ModBlocks.BEANPOD.get().defaultBlockState(), 3);
                }
                return;
            }

            BlockPos upPos = pos.above();
            BlockState upState = level.getBlockState(upPos);
            
            // Magic bean stalk breaks blocks in its way (except unbreakable ones)
            if (upState.getDestroySpeed(level, upPos) != -1.0F) {
                if (!level.isEmptyBlock(upPos)) {
                    level.levelEvent(2001, upPos, Block.getId(upState));
                } else {
                    level.levelEvent(2001, pos, Block.getId(this.defaultBlockState()));
                    level.playSound(null, pos, this.getSoundType(state, level, pos, null).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 2.0F);
                }

                level.levelEvent(2005, upPos, 0);
                level.setBlock(upPos, this.defaultBlockState(), 3);
                level.scheduleTick(upPos, this, 1); // Very fast growth
            } else {
                // If we can't break the block above, place bean pod here instead
                level.setBlock(pos, ModBlocks.BEANPOD.get().defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 1); // Start growing immediately
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
               belowBlock instanceof BlockSpecialBeanStalk;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }
}