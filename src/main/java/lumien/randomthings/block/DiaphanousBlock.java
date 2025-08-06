package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.DiaphanousBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DiaphanousBlock extends BaseEntityBlock {
    public static final MapCodec<DiaphanousBlock> CODEC = simpleCodec(DiaphanousBlock::new);
    
    private static final VoxelShape EMPTY_SHAPE = Shapes.empty();

    public DiaphanousBlock(Properties properties) {
        super(properties
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DiaphanousBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            return diaphanousEntity.isInverted() ? Shapes.block() : EMPTY_SHAPE;
        }
        return EMPTY_SHAPE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Check if player is holding a diaphanous block
        if (context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext entityContext) {
            if (entityContext.getEntity() instanceof Player player) {
                for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
                    ItemStack held = player.getItemInHand(hand);
                    if (!held.isEmpty() && held.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                        return Shapes.block();
                    }
                }
            }
        }
        return EMPTY_SHAPE;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        
        ResourceLocation blockId = stack.get(lumien.randomthings.item.ModDataComponents.DIAPHANOUS_BLOCK_STATE.get());
        Boolean inverted = stack.get(lumien.randomthings.item.ModDataComponents.DIAPHANOUS_INVERTED.get());
        
        if (blockId != null) {
            BlockState toDisplay;
            try {
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockId);
                if (block != null) {
                    toDisplay = block.defaultBlockState();
                } else {
                    toDisplay = Blocks.STONE.defaultBlockState();
                }
            } catch (Exception e) {
                toDisplay = Blocks.STONE.defaultBlockState();
            }
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
                diaphanousEntity.setDisplayState(toDisplay);
                diaphanousEntity.setInverted(inverted != null ? inverted : false);
                diaphanousEntity.setChanged();
            }
        }
        
        if (!level.isClientSide) {
            neighborChanged(state, level, pos, this, pos, false);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            diaphanousEntity.updateRenderMap();
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            BlockState displayState = diaphanousEntity.getDisplayState();
            if (displayState != null && displayState.getBlock() != Blocks.AIR) {
                // Add particles from the displayed block
                displayState.getBlock().animateTick(displayState, level, pos, random);
            }
        }
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            BlockState displayState = diaphanousEntity.getDisplayState();
            if (displayState != null) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, displayState), 
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                    numberOfParticles, 0.0D, 0.0D, 0.0D, 0.15);
                return true;
            }
        }
        return super.addLandingEffects(state1, level, pos, state2, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            BlockState displayState = diaphanousEntity.getDisplayState();
            if (displayState != null) {
                return displayState.getBlock().addRunningEffects(displayState, level, pos, entity);
            }
        }
        return super.addRunningEffects(state, level, pos, entity);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        // Don't call super to prevent default drops
        if (blockEntity instanceof DiaphanousBlockEntity diaphanousEntity) {
            ItemStack drop = new ItemStack(this);
            
            BlockState displayState = diaphanousEntity.getDisplayState();
            if (displayState != null) {
                ResourceLocation blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(displayState.getBlock());
                drop.set(lumien.randomthings.item.ModDataComponents.DIAPHANOUS_BLOCK_STATE.get(), blockId);
            } else {
                drop.set(lumien.randomthings.item.ModDataComponents.DIAPHANOUS_BLOCK_STATE.get(), 
                        ResourceLocation.fromNamespaceAndPath("minecraft", "stone"));
            }
            
            drop.set(lumien.randomthings.item.ModDataComponents.DIAPHANOUS_INVERTED.get(), 
                    diaphanousEntity.isInverted());
            
            popResource(level, pos, drop);
        }
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public boolean canOcclude(BlockState state) {
        return false;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
}