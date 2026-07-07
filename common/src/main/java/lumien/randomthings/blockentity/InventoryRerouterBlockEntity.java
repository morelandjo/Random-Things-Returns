package lumien.randomthings.blockentity;

import lumien.randomthings.block.InventoryRerouterBlock;
import lumien.randomthings.util.RTContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Inventory Rerouter — a transparent sided proxy. To other inventories/hoppers it looks like the
 * inventory behind it ({@code FACING}), but each face can be remapped to a different face of that
 * target. Implemented as a vanilla {@link WorldlyContainer} so vanilla hoppers route through it on
 * both loaders without any platform-specific capability code.
 */
public class InventoryRerouterBlockEntity extends BlockEntity implements WorldlyContainer {
    private static final ThreadLocal<Set<BlockPos>> REENTRY_GUARD = ThreadLocal.withInitial(HashSet::new);
    private static final int[] NO_SLOTS = new int[0];

    private final EnumMap<Direction, Direction> facingMap = new EnumMap<>(Direction.class);

    public InventoryRerouterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.INVENTORY_REROUTER.get(), pos, state);
        for (Direction d : Direction.values()) {
            facingMap.put(d, d);
        }
    }

    @Nullable
    public Direction getOverride(Direction side) {
        return facingMap.get(side);
    }

    public EnumMap<Direction, Direction> getFacingMap() {
        return facingMap;
    }

    /** Cycles the mapping for {@code face}: null → DOWN → UP → NORTH → SOUTH → WEST → EAST → null … */
    public void rotateFace(Direction face) {
        Direction current = facingMap.get(face);
        Direction next;
        if (current == null) {
            next = Direction.DOWN;
        } else if (current == Direction.EAST) {
            next = null;
        } else {
            next = Direction.values()[current.ordinal() + 1];
        }
        facingMap.put(face, next);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    // --- proxy plumbing ---

    @Nullable
    private Direction remap(Direction side) {
        Direction myFacing = getBlockState().getValue(InventoryRerouterBlock.FACING);
        if (side == myFacing) {
            return null; // never expose the back face (keeps the target queryable on its own)
        }
        return facingMap.get(side);
    }

    @Nullable
    private Container liveTarget() {
        if (level == null) {
            return null;
        }
        Direction myFacing = getBlockState().getValue(InventoryRerouterBlock.FACING);
        return RTContainers.getContainerAt(level, worldPosition.relative(myFacing));
    }

    /** Run {@code fn} against the live target, guarding against rerouter→rerouter recursion. */
    private <T> T guarded(T fallback, Function<Container, T> fn) {
        Set<BlockPos> guard = REENTRY_GUARD.get();
        if (!guard.add(worldPosition)) {
            return fallback;
        }
        try {
            Container target = liveTarget();
            return target == null ? fallback : fn.apply(target);
        } finally {
            guard.remove(worldPosition);
        }
    }

    // --- WorldlyContainer (delegates to the live target) ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        return guarded(NO_SLOTS, target -> {
            Direction override = remap(side);
            if (override == null) {
                return NO_SLOTS;
            }
            if (target instanceof WorldlyContainer worldly) {
                return worldly.getSlotsForFace(override);
            }
            int[] all = new int[target.getContainerSize()];
            for (int i = 0; i < all.length; i++) all[i] = i;
            return all;
        });
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return guarded(false, target -> {
            Direction override = side == null ? null : remap(side);
            if (side != null && override == null) {
                return false;
            }
            if (target instanceof WorldlyContainer worldly) {
                return worldly.canPlaceItemThroughFace(slot, stack, override);
            }
            return target.canPlaceItem(slot, stack);
        });
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return guarded(false, target -> {
            Direction override = remap(side);
            if (override == null) {
                return false;
            }
            if (target instanceof WorldlyContainer worldly) {
                return worldly.canTakeItemThroughFace(slot, stack, override);
            }
            return true;
        });
    }

    @Override
    public int getContainerSize() {
        return guarded(0, Container::getContainerSize);
    }

    @Override
    public boolean isEmpty() {
        return guarded(true, Container::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return guarded(ItemStack.EMPTY, target -> target.getItem(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return guarded(ItemStack.EMPTY, target -> target.removeItem(slot, amount));
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return guarded(ItemStack.EMPTY, target -> target.removeItemNoUpdate(slot));
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        guarded(null, target -> {
            target.setItem(slot, stack);
            return null;
        });
    }

    @Override
    public int getMaxStackSize() {
        return guarded(64, Container::getMaxStackSize);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return guarded(false, target -> target.canPlaceItem(slot, stack));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        guarded(null, target -> {
            target.setChanged();
            return null;
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return false; // not a player-openable inventory
    }

    @Override
    public void clearContent() {
        guarded(null, target -> {
            target.clearContent();
            return null;
        });
    }

    // --- NBT / sync ---

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        int[] data = new int[Direction.values().length];
        for (int i = 0; i < data.length; i++) {
            Direction override = facingMap.get(Direction.values()[i]);
            data[i] = override == null ? -1 : override.ordinal();
        }
        tag.putIntArray("facingList", data);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("facingList")) {
            int[] data = tag.getIntArray("facingList");
            Direction[] all = Direction.values();
            for (int i = 0; i < Math.min(data.length, all.length); i++) {
                facingMap.put(all[i], data[i] == -1 ? null : all[data[i]]);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
