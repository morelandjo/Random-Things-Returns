package lumien.randomthings.blockentity;

import lumien.randomthings.block.InventoryRerouterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class InventoryRerouterBlockEntity extends BlockEntity {
    private static final ThreadLocal<Set<BlockPos>> REENTRY_GUARD = ThreadLocal.withInitial(HashSet::new);

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

    /**
     * Returns the forwarded IItemHandler for a request on {@code side} of this rerouter.
     * Returns null when the side is the back-facing direction (so the wrapped inventory
     * stays queryable separately), when the mapping for the side is empty, or when the
     * target inventory is absent.
     */
    @Nullable
    public IItemHandler getMappedHandler(@Nullable Direction side) {
        if (level == null || side == null) return null;

        Direction myFacing = getBlockState().getValue(InventoryRerouterBlock.FACING);
        if (side == myFacing) return null;

        Direction override = facingMap.get(side);
        if (override == null) return null;

        Set<BlockPos> guard = REENTRY_GUARD.get();
        if (!guard.add(worldPosition)) return null;
        try {
            BlockPos targetPos = worldPosition.relative(myFacing);
            return level.getCapability(Capabilities.ItemHandler.BLOCK, targetPos, override);
        } finally {
            guard.remove(worldPosition);
        }
    }

    /**
     * Cycles the mapping for {@code face} through: null → DOWN → UP → NORTH → SOUTH → WEST → EAST → null …
     */
    public void rotateFace(Direction face) {
        Direction current = facingMap.get(face);
        Direction next;
        if (current == null) {
            next = Direction.DOWN;
        } else if (current == Direction.EAST) {
            next = null;
        } else {
            Direction[] all = Direction.values();
            next = all[current.ordinal() + 1];
        }
        facingMap.put(face, next);
        setChanged();
        if (level != null && !level.isClientSide) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        int[] data = new int[Direction.values().length];
        for (int i = 0; i < data.length; i++) {
            Direction d = Direction.values()[i];
            Direction override = facingMap.get(d);
            data[i] = override == null ? -1 : override.ordinal();
        }
        tag.putIntArray("facingList", data);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("facingList")) {
            int[] data = tag.getIntArray("facingList");
            Direction[] all = Direction.values();
            for (int i = 0; i < Math.min(data.length, all.length); i++) {
                facingMap.put(all[i], data[i] == -1 ? null : all[data[i]]);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
