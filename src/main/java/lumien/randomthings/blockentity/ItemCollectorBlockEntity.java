package lumien.randomthings.blockentity;

import lumien.randomthings.block.ItemCollectorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

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
        IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, facing);
        if (target == null) {
            be.relax();
            return;
        }

        boolean didSomething = false;
        for (ItemEntity ei : items) {
            if (!ei.isAlive()) continue;
            ItemStack original = ei.getItem().copy();
            ItemStack left = ItemHandlerHelper.insertItemStacked(target, original, false);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("currentTickRate", currentTickRate);
        tag.putInt("counter", counter);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("currentTickRate")) currentTickRate = tag.getInt("currentTickRate");
        if (tag.contains("counter")) counter = tag.getInt("counter");
        if (currentTickRate < 1) currentTickRate = 1;
        if (currentTickRate > MAX_TICK_RATE) currentTickRate = MAX_TICK_RATE;
    }
}
