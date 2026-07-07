package lumien.randomthings.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lumien.randomthings.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * A bucket that flood-fills from the clicked fluid to the nearest connected <em>source</em> block
 * and picks that up instead of doing nothing on flowing fluid.
 */
public class EnderBucketItem extends Item {

    private static final int CAPACITY = EnderBucketFluidHelper.BUCKET_VOLUME;

    public EnderBucketItem() {
        super(new Item.Properties().stacksTo(16));
    }

    @Override
    public Component getName(ItemStack stack) {
        var fluid = EnderBucketFluidHelper.getFluid(stack);
        if (fluid != Fluids.EMPTY) {
            return Component.translatable("item.randomthings.ender_bucket.filled.name",
                EnderBucketFluidHelper.fluidName(fluid));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.randomthings.ender_bucket.info"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        boolean isFull = EnderBucketFluidHelper.getAmount(itemStack) >= CAPACITY;

        if (isFull) {
            // Try to place the fluid
            BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, false);
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(itemStack);
            }

            BlockPos clickPos = hitResult.getBlockPos();
            if (level.mayInteract(player, clickPos)) {
                BlockPos targetPos = clickPos.relative(hitResult.getDirection());

                if (player.mayUseItemAt(targetPos, hitResult.getDirection(), itemStack)
                    && EnderBucketFluidHelper.placeFluid(player, level, targetPos, itemStack)) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.success(itemStack);
                }
            }

            return InteractionResultHolder.fail(itemStack);
        }

        // Try to pick up fluid: flood-fill from the hit position to the nearest source block
        BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, true);

        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();

            if (level.getBlockState(hitPos).getFluidState().getType() != Fluids.EMPTY) {
                List<BlockPos> toCheck = new ArrayList<>();
                Set<BlockPos> alreadyChecked = new HashSet<>();

                toCheck.add(hitPos);

                while (!toCheck.isEmpty() && alreadyChecked.size() < 2000) {
                    BlockPos next = toCheck.remove(0);
                    alreadyChecked.add(next);

                    if (!level.isLoaded(next)) {
                        continue;
                    }
                    var fluidState = level.getBlockState(next).getFluidState();
                    if (fluidState.getType() == Fluids.EMPTY) {
                        continue;
                    }

                    if (fluidState.isSource()
                        && EnderBucketFluidHelper.canPickUp(itemStack, fluidState.getType(), CAPACITY)
                        && !level.isClientSide) {

                        if (itemStack.getCount() > 1) {
                            // Split one bucket off the stack and fill it
                            ItemStack returnStack = itemStack.copy();
                            returnStack.shrink(1);

                            ItemStack filled = itemStack.copyWithCount(1);
                            if (EnderBucketFluidHelper.pickUpSource(filled, player, level, next)) {
                                if (!player.getInventory().add(filled)) {
                                    player.drop(filled, false);
                                }
                                return InteractionResultHolder.success(returnStack);
                            }
                        } else if (EnderBucketFluidHelper.pickUpSource(itemStack, player, level, next)) {
                            return InteractionResultHolder.success(itemStack);
                        }
                    }

                    for (Direction facing : Direction.values()) {
                        BlockPos neighbor = next.relative(facing);
                        if (!alreadyChecked.contains(neighbor)) {
                            toCheck.add(neighbor);
                        }
                    }
                }
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
