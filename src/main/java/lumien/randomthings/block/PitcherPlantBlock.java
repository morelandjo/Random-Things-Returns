package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.world.level.material.Fluids;

public class PitcherPlantBlock extends BushBlock {
    public static final MapCodec<PitcherPlantBlock> CODEC = simpleCodec(properties -> new PitcherPlantBlock());

    @Override
    public MapCodec<PitcherPlantBlock> codec() {
        return CODEC;
    }

    protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 15.0D, 11.0D);

    public PitcherPlantBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .instabreak()
            .sound(SoundType.CROP)
            .randomTicks());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || super.mayPlaceOn(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Try both hands when interacting without item
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.isEmpty()) {
                InteractionResult result = handleInteraction(state, level, pos, player, hitResult, heldItem, hand);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return InteractionResult.PASS;
    }

    private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, ItemStack heldItem, InteractionHand hand) {
        // Handle glass bottles
        if (heldItem.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide) {
                // Create water bottle
                ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                
                // Replace or add to inventory
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, waterBottle);
                } else if (!player.getInventory().add(waterBottle)) {
                    player.drop(waterBottle, false);
                }
                
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        // Handle buckets specifically
        if (heldItem.is(Items.BUCKET)) {
            if (!level.isClientSide) {
                // Replace empty bucket with water bucket
                ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, waterBucket);
                } else if (!player.getInventory().add(waterBucket)) {
                    player.drop(waterBucket, false);
                }
                
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        
        // Handle fluid containers via capability
        IFluidHandler fluidHandler = heldItem.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            // Try to fill the container with water
            FluidStack waterStack = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
            int filled = fluidHandler.fill(waterStack, IFluidHandler.FluidAction.SIMULATE);
            
            if (filled > 0) {
                if (!level.isClientSide) {
                    ItemStack resultContainer = heldItem.copy();
                    IFluidHandler resultHandler = resultContainer.getCapability(Capabilities.FluidHandler.ITEM);
                    if (resultHandler != null) {
                        int actualFilled = resultHandler.fill(new FluidStack(Fluids.WATER, filled), IFluidHandler.FluidAction.EXECUTE);
                        if (actualFilled > 0) {
                            if (!player.getAbilities().instabuild) {
                                heldItem.shrink(1);
                            }
                            
                            if (heldItem.isEmpty()) {
                                player.setItemInHand(hand, resultContainer);
                            } else if (!player.getInventory().add(resultContainer)) {
                                player.drop(resultContainer, false);
                            }
                            
                            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Fill adjacent cauldrons and fluid tanks
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            
            // Handle cauldrons
            if (adjacentState.getBlock() instanceof LayeredCauldronBlock) {
                int currentLevel = adjacentState.getValue(LayeredCauldronBlock.LEVEL);
                if (currentLevel < 3) {
                    level.setBlock(adjacentPos, adjacentState.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1), 3);
                    level.playSound(null, adjacentPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
                }
            }
            // Handle empty cauldrons
            else if (adjacentState.is(Blocks.CAULDRON)) {
                level.setBlock(adjacentPos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1), 3);
                level.playSound(null, adjacentPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            // Handle fluid tanks (block entities with fluid capability)
            else {
                BlockEntity blockEntity = level.getBlockEntity(adjacentPos);
                if (blockEntity != null) {
                    IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, adjacentPos, direction.getOpposite());
                    if (fluidHandler != null) {
                        FluidStack waterStack = new FluidStack(Fluids.WATER, 1000);
                        int filled = fluidHandler.fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
                        if (filled > 0) {
                            level.playSound(null, adjacentPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.3F, 1.0F);
                        }
                    }
                }
            }
        }
    }
}