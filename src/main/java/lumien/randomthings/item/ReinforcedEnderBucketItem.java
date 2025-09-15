package lumien.randomthings.item;

import lumien.randomthings.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ReinforcedEnderBucketItem extends Item {

    private static final int CAPACITY = 10000; // 10 buckets worth

    public ReinforcedEnderBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack contained = fluidHandler.drain(CAPACITY, IFluidHandler.FluidAction.SIMULATE);
            if (contained != null && !contained.isEmpty()) {
                // Return color based on fluid type
                if (contained.getFluid() == Fluids.WATER) {
                    return Color.BLUE.getRGB();
                } else if (contained.getFluid() == Fluids.LAVA) {
                    return Color.ORANGE.getRGB();
                }
                // Add more fluid colors as needed
            }
        }
        return super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack contained = fluidHandler.drain(CAPACITY, IFluidHandler.FluidAction.SIMULATE);
            float filledPercent = 0;
            if (contained != null && !contained.isEmpty()) {
                filledPercent = contained.getAmount() / (float) CAPACITY;
            }
            return Math.round(13.0F * filledPercent);
        }
        return 0;
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack fluidStack = fluidHandler.drain(1, IFluidHandler.FluidAction.SIMULATE);
            if (fluidStack != null && !fluidStack.isEmpty()) {
                return Component.translatable("item.randomthings.reinforced_ender_bucket.filled.name", fluidStack.getFluid().getFluidType().getDescription());
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.randomthings.reinforced_ender_bucket.info"));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        IFluidHandlerItem fluidHandler = itemStack.getCapability(Capabilities.FluidHandler.ITEM);

        if (fluidHandler == null) {
            return InteractionResultHolder.pass(itemStack);
        }

        BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, true);

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            BlockState hitState = level.getBlockState(hitPos);
            Block hitBlock = hitState.getBlock();

            boolean collectAll = player.isShiftKeyDown();

            if (hitBlock instanceof LiquidBlock || hitState.getFluidState().getType() != Fluids.EMPTY) {
                // Search for connected fluid blocks
                List<BlockPos> toCheck = new ArrayList<>();
                Set<BlockPos> alreadyChecked = new HashSet<>();

                toCheck.add(hitPos);

                while (!toCheck.isEmpty() && alreadyChecked.size() < 2000) {
                    BlockPos next = toCheck.remove(0);
                    alreadyChecked.add(next);

                    if (level.isLoaded(next)) {
                        BlockState nextState = level.getBlockState(next);
                        Block nextBlock = nextState.getBlock();

                        if (nextBlock instanceof LiquidBlock || nextState.getFluidState().getType() != Fluids.EMPTY) {
                            FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(itemStack, player, level, next, Direction.UP);

                            if (pickupResult.isSuccess()) {
                                if (!collectAll) {
                                    return InteractionResultHolder.success(pickupResult.getResult());
                                } else {
                                    itemStack = pickupResult.getResult();
                                }
                            }

                            // Add neighboring positions to check
                            for (Direction facing : Direction.values()) {
                                BlockPos neighbor = next.relative(facing);
                                if (!alreadyChecked.contains(neighbor)) {
                                    toCheck.add(neighbor);
                                }
                            }
                        }
                    }
                }

                if (collectAll) {
                    return InteractionResultHolder.success(itemStack);
                }
            } else {
                // Try to place fluid
                FluidStack fluidStack = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);

                if (fluidStack != null && fluidStack.getAmount() >= 1000) {
                    BlockPos clickPos = hitResult.getBlockPos();

                    if (level.mayInteract(player, clickPos)) {
                        BlockPos targetPos = clickPos.relative(hitResult.getDirection());

                        if (player.mayUseItemAt(targetPos, hitResult.getDirection(), itemStack)) {
                            FluidActionResult result = FluidUtil.tryPlaceFluid(player, level, hand, targetPos, itemStack, fluidStack);
                            if (result.isSuccess()) {
                                player.awardStat(Stats.ITEM_USED.get(this));
                                return InteractionResultHolder.success(result.getResult());
                            }
                        }
                    }
                }

                return InteractionResultHolder.fail(itemStack);
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}