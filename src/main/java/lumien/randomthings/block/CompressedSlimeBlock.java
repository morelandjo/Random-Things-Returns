package lumien.randomthings.block;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class CompressedSlimeBlock extends Block {
    public static final IntegerProperty COMPRESSION = IntegerProperty.create("compression", 0, 2);
    
    protected static final VoxelShape AABB_0 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape AABB_1 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    protected static final VoxelShape AABB_2 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public CompressedSlimeBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(0.5F)
            .sound(SoundType.SLIME_BLOCK)
            .friction(0.8F)
            .jumpFactor(1.0F)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(COMPRESSION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COMPRESSION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int compression = state.getValue(COMPRESSION);
        return switch (compression) {
            case 0 -> AABB_0;
            case 1 -> AABB_1;
            case 2 -> AABB_2;
            default -> AABB_0;
        };
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        if (heldItem.getItem() instanceof ShovelItem && !level.isClientSide) {
            int currentCompression = state.getValue(COMPRESSION);
            
            if (currentCompression < 2) {
                BlockState newState = state.setValue(COMPRESSION, currentCompression + 1);
                level.setBlock(pos, newState, 3);
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F + (currentCompression * 0.1F));
                
                if (!player.isCreative()) {
                    heldItem.hurtAndBreak(1, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
                }
                
                return InteractionResult.SUCCESS;
            }
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(Blocks.SLIME_BLOCK);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.getDeltaMovement().y < 1.0) {
            int compression = state.getValue(COMPRESSION);
            
            entity.setOnGround(false);
            entity.fallDistance = 0;
            entity.setDeltaMovement(entity.getDeltaMovement().x, 0.8 + compression * 0.4, entity.getDeltaMovement().z);
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // Don't take fall damage on compressed slime blocks
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
    }
}