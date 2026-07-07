package lumien.randomthings.blockentity;

import lumien.randomthings.block.ItemCollectorBlock;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ItemCollectorBlockEntity extends BlockEntity {
    public static final int RANGE = 3;
    private static final int MAX_TICK_RATE = 20;

    private int currentTickRate = MAX_TICK_RATE;
    private int counter = 0;

    public ItemCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ITEM_COLLECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemCollectorBlockEntity be) {
        if (level.isClientSide) return;

        be.counter++;
        if (be.counter < be.currentTickRate) return;
        be.counter = 0;

        Direction facing = state.getValue(ItemCollectorBlock.FACING);
        AABB scanBox = new AABB(
            pos.getX() - RANGE, pos.getY() - RANGE, pos.getZ() - RANGE,
            pos.getX() + RANGE + 1, pos.getY() + RANGE + 1, pos.getZ() + RANGE + 1);

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanBox, ItemEntity::isAlive);
        if (items.isEmpty()) {
            be.relax();
            return;
        }

        BlockPos targetPos = pos.relative(facing.getOpposite());
        Container target = RTContainers.getContainerAt(level, targetPos);
        if (target == null) {
            be.relax();
            return;
        }

        // Items enter the target through the face pointing back at the collector.
        Direction insertFace = facing;

        boolean didSomething = false;
        for (ItemEntity ei : items) {
            if (!ei.isAlive()) continue;
            ItemStack original = ei.getItem().copy();
            ItemStack left = RTContainers.insert(target, original, insertFace);
            if (left.getCount() < original.getCount()) {
                didSomething = true;
            }
            if (left.isEmpty()) {
                ei.discard();
            } else {
                ei.setItem(left);
            }
        }

        if (didSomething) {
            if (be.currentTickRate > 1) be.currentTickRate--;
        } else {
            be.relax();
        }
    }

    private void relax() {
        if (currentTickRate < MAX_TICK_RATE) currentTickRate++;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("currentTickRate", currentTickRate);
        tag.putInt("counter", counter);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("currentTickRate")) currentTickRate = tag.getInt("currentTickRate");
        if (tag.contains("counter")) counter = tag.getInt("counter");
        if (currentTickRate < 1) currentTickRate = 1;
        if (currentTickRate > MAX_TICK_RATE) currentTickRate = MAX_TICK_RATE;
    }
}
