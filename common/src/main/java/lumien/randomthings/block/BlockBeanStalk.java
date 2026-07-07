package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Climbable beanstalk (via the {@code minecraft:climbable} tag) that grows steadily upward. */
public class BlockBeanStalk extends Block {
    protected static final VoxelShape STALK_AABB = Block.box(6.4, 0, 6.4, 9.6, 16, 9.6);

    public BlockBeanStalk() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT).strength(0.4F).sound(SoundType.LADDER)
            .noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return STALK_AABB;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.onGround() || entity.verticalCollision) {
            return;
        }
        double speed = 0.2;
        if (entity.getDeltaMovement().y >= 0.1) {
            BlockPos topPos = new BlockPos(Mth.floor(entity.getX()), Mth.floor(entity.getY()) + 3, Mth.floor(entity.getZ()));
            if (level.getBlockState(topPos).getBlock() == this) {
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
        if (level.isClientSide || pos.getY() >= level.getMaxBuildHeight() || !level.isEmptyBlock(pos.above())) {
            return;
        }
        level.levelEvent(2001, pos, Block.getId(this.defaultBlockState()));
        level.playSound(null, pos, this.getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 2.0F);
        level.levelEvent(2005, pos.above(), 0);
        level.setBlock(pos.above(), this.defaultBlockState(), 3);
        level.scheduleTick(pos.above(), this, 5);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
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
        Block belowBlock = level.getBlockState(belowPos).getBlock();
        return belowBlock == Blocks.GRASS_BLOCK || belowBlock == Blocks.DIRT || belowBlock == Blocks.COARSE_DIRT
            || belowBlock == Blocks.PODZOL || belowBlock == Blocks.ROOTED_DIRT || belowBlock instanceof BlockBeanStalk;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (!this.canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }
}
