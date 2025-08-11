package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import lumien.randomthings.blockentity.FluidDisplayBlockEntity;
import lumien.randomthings.blockentity.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class FluidDisplayBlock extends BaseEntityBlock {
    
    public static final MapCodec<FluidDisplayBlock> CODEC = simpleCodec(properties -> new FluidDisplayBlock());
    
    public FluidDisplayBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.3f)
                .sound(SoundType.GLASS)
                .noOcclusion());
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FluidDisplayBlockEntity fluidDisplay) {
            ItemStack heldItem = player.getMainHandItem();
            
            // Check if item has fluid capability
            IFluidHandler fluidHandler = heldItem.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandler != null && !heldItem.isEmpty()) {
                // Try to get fluid from the first tank
                FluidStack fluid = fluidHandler.getFluidInTank(0);
                if (!fluid.isEmpty()) {
                    if (!level.isClientSide) {
                        FluidStack displayFluid = fluid.copy();
                        displayFluid.setAmount(1000);
                        fluidDisplay.setFluidStack(displayFluid);
                        fluidDisplay.setChanged();
                        fluidDisplay.syncToClients();
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            
            // No fluid container - toggle flowing/rotation
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    fluidDisplay.cycleRotation();
                } else {
                    fluidDisplay.toggleFlowing();
                }
            }
            return InteractionResult.SUCCESS;
        }
        
        return InteractionResult.PASS;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDisplayBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntityTypes.FLUID_DISPLAY.get(), FluidDisplayBlockEntity::tick);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }
}