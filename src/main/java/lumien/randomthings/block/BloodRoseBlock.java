package lumien.randomthings.block;

import lumien.randomthings.blockentity.BloodRoseBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloodRoseBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<BloodRoseBlock> CODEC = simpleCodec(properties -> new BloodRoseBlock());

    @Override
    public MapCodec<BloodRoseBlock> codec() {
        return CODEC;
    }

    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 15.0D, 11.0D);

    public BloodRoseBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0F)
            .sound(SoundType.GRASS)
            .noCollission()
            .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(level, pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodRoseBlockEntity(pos, state);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        } else {
            return blockEntityType == ModBlockEntityTypes.BLOOD_ROSE.get() ? 
                (BlockEntityTicker<T>) (BlockEntityTicker<BloodRoseBlockEntity>) BloodRoseBlockEntity::tick : null;
        }
    }
}