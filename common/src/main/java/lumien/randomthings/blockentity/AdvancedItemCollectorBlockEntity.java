package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.block.AdvancedItemCollectorBlock;
import lumien.randomthings.item.ItemFilterItem;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedItemCollectorBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    public static final int MAX_RANGE = 10;
    private static final int MAX_TICK_RATE = 20;

    private int currentTickRate = MAX_TICK_RATE;
    private int counter = 0;

    private int rangeX = 5;
    private int rangeY = 5;
    private int rangeZ = 5;

    private final SimpleContainer filterSlot = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stack.isEmpty() || stack.getItem() instanceof ItemFilterItem;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            AdvancedItemCollectorBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> rangeX;
                case 1 -> rangeY;
                case 2 -> rangeZ;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            int clamped = clampRange(value);
            switch (index) {
                case 0 -> rangeX = clamped;
                case 1 -> rangeY = clamped;
                case 2 -> rangeZ = clamped;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public AdvancedItemCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ADVANCED_ITEM_COLLECTOR.get(), pos, state);
    }

    public static int clampRange(int v) {
        return Math.max(0, Math.min(MAX_RANGE, v));
    }

    public int getRangeX() { return rangeX; }
    public int getRangeY() { return rangeY; }
    public int getRangeZ() { return rangeZ; }
    public SimpleContainer getFilterSlot() { return filterSlot; }

    public void setRange(int axis, int value) {
        int clamped = clampRange(value);
        switch (axis) {
            case 0 -> rangeX = clamped;
            case 1 -> rangeY = clamped;
            case 2 -> rangeZ = clamped;
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedItemCollectorBlockEntity be) {
        if (level.isClientSide) return;

        be.counter++;
        if (be.counter < be.currentTickRate) return;
        be.counter = 0;

        Direction facing = state.getValue(AdvancedItemCollectorBlock.FACING);
        AABB scanBox = new AABB(
            pos.getX() - be.rangeX, pos.getY() - be.rangeY, pos.getZ() - be.rangeZ,
            pos.getX() + be.rangeX + 1, pos.getY() + be.rangeY + 1, pos.getZ() + be.rangeZ + 1);

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
        Direction insertFace = facing;

        ItemStack filterStack = be.filterSlot.getItem(0);
        boolean hasFilter = !filterStack.isEmpty() && filterStack.getItem() instanceof ItemFilterItem;

        boolean didSomething = false;
        for (ItemEntity ei : items) {
            if (!ei.isAlive()) continue;
            ItemStack candidate = ei.getItem();
            if (hasFilter && !ItemFilterItem.matchesFilter(filterStack, candidate)) continue;

            ItemStack original = candidate.copy();
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
        tag.putInt("rangeX", rangeX);
        tag.putInt("rangeY", rangeY);
        tag.putInt("rangeZ", rangeZ);
        tag.putInt("currentTickRate", currentTickRate);
        tag.putInt("counter", counter);
        ItemStack s = filterSlot.getItem(0);
        if (!s.isEmpty()) {
            tag.put("FilterItem", s.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rangeX = tag.contains("rangeX") ? clampRange(tag.getInt("rangeX")) : 5;
        rangeY = tag.contains("rangeY") ? clampRange(tag.getInt("rangeY")) : 5;
        rangeZ = tag.contains("rangeZ") ? clampRange(tag.getInt("rangeZ")) : 5;
        if (tag.contains("currentTickRate")) currentTickRate = tag.getInt("currentTickRate");
        if (tag.contains("counter")) counter = tag.getInt("counter");
        if (currentTickRate < 1) currentTickRate = 1;
        if (currentTickRate > MAX_TICK_RATE) currentTickRate = MAX_TICK_RATE;
        filterSlot.setItem(0, tag.contains("FilterItem") ? ItemStack.of(tag.getCompound("FilterItem")) : ItemStack.EMPTY);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("rangeX", rangeX);
        tag.putInt("rangeY", rangeY);
        tag.putInt("rangeZ", rangeZ);
        ItemStack s = filterSlot.getItem(0);
        if (!s.isEmpty()) {
            tag.put("FilterItem", s.save(new CompoundTag()));
        }
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.advanced_item_collector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new lumien.randomthings.menu.AdvancedItemCollectorMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.level != null) {
            ItemStack stack = filterSlot.getItem(0);
            if (!stack.isEmpty()) {
                Block.popResource(this.level, this.worldPosition, stack);
                filterSlot.setItem(0, ItemStack.EMPTY);
            }
        }
    }
}
