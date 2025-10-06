package lumien.randomthings.handler.redstonesignal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RedstoneSignalHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_redstonesignals";

    private List<RedstoneSignal> redstoneSignals;

    public RedstoneSignalHandler() {
        this.redstoneSignals = new ArrayList<>();
    }

    public RedstoneSignalHandler(CompoundTag compound, HolderLookup.Provider registries) {
        this.redstoneSignals = new ArrayList<>();

        ListTag signalList = compound.getList("redstoneSignals", Tag.TAG_COMPOUND);
        for (int i = 0; i < signalList.size(); i++) {
            CompoundTag signalCompound = signalList.getCompound(i);
            RedstoneSignal signal = new RedstoneSignal();
            signal.readFromNBT(signalCompound);
            this.redstoneSignals.add(signal);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        ListTag signalList = new ListTag();

        for (RedstoneSignal signal : redstoneSignals) {
            CompoundTag signalCompound = new CompoundTag();
            signal.writeToNBT(signalCompound);
            signalList.add(signalCompound);
        }

        compound.put("redstoneSignals", signalList);
        return compound;
    }

    public static RedstoneSignalHandler get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(RedstoneSignalHandler::new, RedstoneSignalHandler::new),
                DATA_NAME
            );
        }
        return new RedstoneSignalHandler();
    }

    private void updatePosition(Level level, BlockPos pos) {
        BlockState targetState = level.getBlockState(pos);
        // Trigger neighbor updates to notify the block of the redstone signal change
        level.updateNeighborsAt(pos, Blocks.REDSTONE_BLOCK);
        // Also update neighbors from all sides
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), Blocks.REDSTONE_BLOCK);
        }
    }

    public synchronized boolean addSignal(Level level, BlockPos pos, int duration, int strength) {
        if (level.isLoaded(pos)) {
            String dimensionKey = level.dimension().location().toString();
            redstoneSignals.add(new RedstoneSignal(dimensionKey, pos, duration, strength));
            updatePosition(level, pos);
            setDirty();
            return true;
        }
        return false;
    }

    public synchronized void tick(MinecraftServer server) {
        Iterator<RedstoneSignal> iterator = redstoneSignals.iterator();

        while (iterator.hasNext()) {
            RedstoneSignal signal = iterator.next();

            // Get the dimension level for this signal
            ServerLevel signalLevel = null;
            for (ServerLevel level : server.getAllLevels()) {
                if (level.dimension().location().toString().equals(signal.getDimension())) {
                    signalLevel = level;
                    break;
                }
            }

            if (signalLevel != null && signalLevel.isLoaded(signal.getPosition())) {
                if (signal.tick()) {
                    iterator.remove();
                    updatePosition(signalLevel, signal.getPosition());
                    setDirty();
                }
            }
        }
    }

    public synchronized int getStrongPower(Level level, BlockPos pos) {
        String dimensionKey = level.dimension().location().toString();

        for (RedstoneSignal signal : redstoneSignals) {
            if (signal.getDimension().equals(dimensionKey)) {
                if (signal.getPosition().equals(pos)) {
                    return signal.getRedstoneStrength();
                }
            }
        }

        return 0;
    }
}
