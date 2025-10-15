package lumien.randomthings.handler.spectrecoil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player Spectre Energy storage globally.
 * Stores up to 1,000,000 RF per player that can be accessed via Spectre Coils and Spectre Chargers.
 */
public class SpectreCoilHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_spectre_coil_handler";
    private static final int MAX_ENERGY = 1000000; // 1 million RF per player

    private final Map<UUID, Integer> coilEntries = new HashMap<>();

    public SpectreCoilHandler() {
        super();
    }

    /**
     * Get the SpectreCoilHandler instance for the given level.
     */
    public static SpectreCoilHandler get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(
                SpectreCoilHandler::new,
                SpectreCoilHandler::load
            ),
            DATA_NAME
        );
    }

    /**
     * Load handler from NBT.
     */
    private static SpectreCoilHandler load(CompoundTag tag, HolderLookup.Provider provider) {
        SpectreCoilHandler handler = new SpectreCoilHandler();

        ListTag list = tag.getList("coilEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            UUID uuid = compound.getUUID("uuid");
            int energy = compound.getInt("energy");
            handler.coilEntries.put(uuid, energy);
        }

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, Integer> entry : coilEntries.entrySet()) {
            CompoundTag entryCompound = new CompoundTag();
            entryCompound.putUUID("uuid", entry.getKey());
            entryCompound.putInt("energy", entry.getValue());
            list.add(entryCompound);
        }

        tag.put("coilEntries", list);
        return tag;
    }

    /**
     * Get an IEnergyStorage for receiving energy into a player's buffer (used by Energy Injector).
     * This storage can only receive energy, not extract it.
     */
    public IEnergyStorage getStorage(UUID owner) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.min(MAX_ENERGY, currentEnergy + maxReceive);

                if (!simulate) {
                    coilEntries.put(owner, newEnergy);
                    setDirty();
                }

                return newEnergy - currentEnergy;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0; // Cannot extract from injector side
            }

            @Override
            public int getEnergyStored() {
                return coilEntries.getOrDefault(owner, 0);
            }

            @Override
            public int getMaxEnergyStored() {
                return MAX_ENERGY;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }

    /**
     * Get an IEnergyStorage for both receiving and extracting energy from a player's buffer.
     * This is used by Spectre Coils and Spectre Chargers.
     */
    public IEnergyStorage getStorageCoil(UUID owner) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.min(MAX_ENERGY, currentEnergy + maxReceive);

                if (!simulate) {
                    coilEntries.put(owner, newEnergy);
                    setDirty();
                }

                return newEnergy - currentEnergy;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.max(0, currentEnergy - maxExtract);

                if (!simulate) {
                    coilEntries.put(owner, newEnergy);
                    setDirty();
                }

                return currentEnergy - newEnergy;
            }

            @Override
            public int getEnergyStored() {
                return coilEntries.getOrDefault(owner, 0);
            }

            @Override
            public int getMaxEnergyStored() {
                return MAX_ENERGY;
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }

    /**
     * Get the current energy stored for a player.
     */
    public int getEnergyStored(UUID owner) {
        return coilEntries.getOrDefault(owner, 0);
    }
}
