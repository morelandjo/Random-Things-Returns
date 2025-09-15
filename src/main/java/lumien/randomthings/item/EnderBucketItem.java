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
import java.util.*;

public class EnderBucketItem extends Item {

    public EnderBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler == null) {
            return 16; // Default when capability isn't present
        }

        FluidStack fluidStack = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (fluidStack != null && !fluidStack.isEmpty()) {
            return 1;
        } else {
            return 16;
        }
    }

    @Override
    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        IFluidHandlerItem fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (fluidHandler != null) {
            FluidStack fluidStack = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (fluidStack != null && !fluidStack.isEmpty()) {
                return Component.translatable("item.randomthings.ender_bucket.filled.name", fluidStack.getFluid().getFluidType().getDescription());
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.randomthings.ender_bucket.info"));
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

        boolean isFull = false;
        FluidStack containedFluid = fluidHandler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        if (containedFluid != null && containedFluid.getAmount() == 1000) {
            isFull = true;
        }

        if (isFull) {
            // Try to place fluid
            BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, false);

            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(itemStack);
            }

            BlockPos clickPos = hitResult.getBlockPos();
            if (level.mayInteract(player, clickPos)) {
                BlockPos targetPos = clickPos.relative(hitResult.getDirection());

                if (player.mayUseItemAt(targetPos, hitResult.getDirection(), itemStack)) {
                    FluidActionResult result = FluidUtil.tryPlaceFluid(player, level, hand, targetPos, itemStack, containedFluid);
                    if (result.isSuccess()) {
                        player.awardStat(Stats.ITEM_USED.get(this));
                        return InteractionResultHolder.success(result.getResult());
                    }
                }
            }

            return InteractionResultHolder.fail(itemStack);
        } else {
            // Try to pick up fluid
            BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, true);

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos hitPos = hitResult.getBlockPos();
                BlockState hitState = level.getBlockState(hitPos);
                Block hitBlock = hitState.getBlock();

                if (hitBlock instanceof LiquidBlock || hitState.getFluidState().getType() != Fluids.EMPTY) {
                    // Search for connected source blocks
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
                                // Only try to pick up actual source blocks
                                if (nextState.getFluidState().isSource()) {
                                    FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(itemStack, player, level, next, Direction.UP);

                                    if (pickupResult.isSuccess()) {
                                        if (itemStack.getCount() > 1) {
                                            ItemStack returnStack = itemStack.copy();
                                            returnStack.shrink(1);

                                            ItemStack add = pickupResult.getResult();
                                            add.setCount(1);

                                            if (!player.getInventory().add(add)) {
                                                player.drop(add, false);
                                            }

                                            return InteractionResultHolder.success(returnStack);
                                        } else {
                                            return InteractionResultHolder.success(pickupResult.getResult());
                                        }
                                    }
                                }

                                // Always add neighboring positions to search, even if pickup failed
                                for (Direction facing : Direction.values()) {
                                    BlockPos neighbor = next.relative(facing);
                                    if (!alreadyChecked.contains(neighbor)) {
                                        toCheck.add(neighbor);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}