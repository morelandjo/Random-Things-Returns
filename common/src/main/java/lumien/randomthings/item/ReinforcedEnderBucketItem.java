package lumien.randomthings.item;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lumien.randomthings.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

/**
 * An Ender Bucket holding 10 buckets of fluid. Sneak-clicking a fluid collects every connected
 * source block until the bucket is full.
 */
public class ReinforcedEnderBucketItem extends Item {

    private static final int CAPACITY = 10 * EnderBucketFluidHelper.BUCKET_VOLUME;

    public ReinforcedEnderBucketItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Fluid fluid = EnderBucketFluidHelper.getFluid(stack);
        if (fluid != Fluids.EMPTY) {
            if (fluid.is(FluidTags.WATER)) {
                return Color.BLUE.getRGB();
            } else if (fluid.is(FluidTags.LAVA)) {
                return Color.ORANGE.getRGB();
            }
        }
        return super.getBarColor(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * EnderBucketFluidHelper.getAmount(stack) / (float) CAPACITY);
    }

    @Override
    public Component getName(ItemStack stack) {
        Fluid fluid = EnderBucketFluidHelper.getFluid(stack);
        if (fluid != Fluids.EMPTY) {
            return Component.translatable("item.randomthings.reinforced_ender_bucket.filled.name",
                EnderBucketFluidHelper.fluidName(fluid));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.randomthings.reinforced_ender_bucket.info"));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        BlockHitResult hitResult = WorldUtil.rayTraceAll(level, player, true);
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemStack);
        }

        BlockPos hitPos = hitResult.getBlockPos();
        boolean collectAll = player.isShiftKeyDown();

        if (level.getBlockState(hitPos).getFluidState().getType() != Fluids.EMPTY) {
            // Flood-fill pickup: one source, or all connected sources while sneaking
            boolean pickedAny = false;
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
                    && !level.isClientSide
                    && EnderBucketFluidHelper.pickUpSource(itemStack, player, level, next)) {

                    if (!collectAll) {
                        return InteractionResultHolder.success(itemStack);
                    }
                    pickedAny = true;
                }

                for (Direction facing : Direction.values()) {
                    BlockPos neighbor = next.relative(facing);
                    if (!alreadyChecked.contains(neighbor)) {
                        toCheck.add(neighbor);
                    }
                }
            }

            if (collectAll && pickedAny) {
                return InteractionResultHolder.success(itemStack);
            }
            return InteractionResultHolder.pass(itemStack);
        }

        // Try to place fluid
        if (EnderBucketFluidHelper.getAmount(itemStack) >= EnderBucketFluidHelper.BUCKET_VOLUME) {
            if (level.mayInteract(player, hitPos)) {
                BlockPos targetPos = hitPos.relative(hitResult.getDirection());

                if (player.mayUseItemAt(targetPos, hitResult.getDirection(), itemStack)
                    && EnderBucketFluidHelper.placeFluid(player, level, targetPos, itemStack)) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.success(itemStack);
                }
            }
            return InteractionResultHolder.fail(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
