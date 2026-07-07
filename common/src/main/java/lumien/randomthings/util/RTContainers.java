package lumien.randomthings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import javax.annotation.Nullable;

/**
 * Cross-loader item-inventory access via vanilla {@link Container} / {@link WorldlyContainer}.
 *
 * <p>1.20.1 has no Architectury item-handler abstraction, and Forge {@code IItemHandler} /
 * Fabric Transfer API {@code Storage} are loader-specific. Vanilla {@link Container} is the common
 * denominator that hoppers and most inventories speak on both loaders, so Random Things' item-moving
 * blocks read neighbours through {@link HopperBlockEntity#getContainerAt} and insert with the
 * sided-aware {@link #insert} helper here.</p>
 *
 * <p>Limitation: blocks that expose <em>only</em> a Forge {@code IItemHandler} or Fabric
 * {@code Storage} (and not a vanilla {@code Container}) are not seen. That covers far fewer
 * inventories than it sounds; the golden path (vanilla containers + our own blocks) works on both
 * loaders with no platform code.</p>
 */
public final class RTContainers {

    private RTContainers() {
    }

    @Nullable
    public static Container getContainerAt(Level level, BlockPos pos) {
        return HopperBlockEntity.getContainerAt(level, pos);
    }

    /**
     * Insert {@code stack} into {@code target} from the given {@code side} (face of the target the
     * items enter through; null = unsided). Returns the leftover that did not fit.
     */
    public static ItemStack insert(@Nullable Container target, ItemStack stack, @Nullable Direction side) {
        if (target == null || stack.isEmpty()) {
            return stack;
        }
        ItemStack remaining = stack;
        if (target instanceof WorldlyContainer worldly && side != null) {
            int[] slots = worldly.getSlotsForFace(side);
            for (int slot : slots) {
                if (remaining.isEmpty()) break;
                if (worldly.canPlaceItemThroughFace(slot, remaining, side)) {
                    remaining = mergeIntoSlot(target, slot, remaining);
                }
            }
        } else {
            for (int slot = 0; slot < target.getContainerSize(); slot++) {
                if (remaining.isEmpty()) break;
                remaining = mergeIntoSlot(target, slot, remaining);
            }
        }
        return remaining;
    }

    private static ItemStack mergeIntoSlot(Container target, int slot, ItemStack stack) {
        if (!target.canPlaceItem(slot, stack)) {
            return stack;
        }
        ItemStack existing = target.getItem(slot);
        int limit = Math.min(target.getMaxStackSize(), stack.getMaxStackSize());

        if (existing.isEmpty()) {
            int move = Math.min(stack.getCount(), limit);
            ItemStack placed = stack.copy();
            placed.setCount(move);
            target.setItem(slot, placed);
            target.setChanged();
            ItemStack leftover = stack.copy();
            leftover.shrink(move);
            return leftover;
        }

        if (ItemStack.isSameItemSameTags(existing, stack)) {
            int space = limit - existing.getCount();
            if (space > 0) {
                int move = Math.min(space, stack.getCount());
                existing.grow(move);
                target.setItem(slot, existing);
                target.setChanged();
                ItemStack leftover = stack.copy();
                leftover.shrink(move);
                return leftover;
            }
        }
        return stack;
    }
}
