package lumien.randomthings.handler.spectrecoil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The per-player Spectre Energy buffer (up to 1,000,000 RF each), shared across all dimensions.
 * Plain {@link SavedData} — completely loader-independent; only the interop seams with other mods'
 * energy go through {@code Services.ENERGY}.
 */
public class SpectreCoilHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_spectre_coil_handler";
    public static final int MAX_ENERGY = 1_000_000;

    private final Map<UUID, Integer> coilEntries = new HashMap<>();

    public static SpectreCoilHandler get(MinecraftServer server) {
        return server.overworld().getDataStorage()
            .computeIfAbsent(SpectreCoilHandler::load, SpectreCoilHandler::new, DATA_NAME);
    }

    private static SpectreCoilHandler load(CompoundTag tag) {
        SpectreCoilHandler handler = new SpectreCoilHandler();
        ListTag list = tag.getList("coilEntries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag compound = list.getCompound(i);
            handler.coilEntries.put(compound.getUUID("uuid"), compound.getInt("energy"));
        }
        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
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

    public int getEnergy(UUID owner) {
        return coilEntries.getOrDefault(owner, 0);
    }

    public void setEnergy(UUID owner, int energy) {
        coilEntries.put(owner, Math.max(0, Math.min(MAX_ENERGY, energy)));
        setDirty();
    }

    /** Inserts up to {@code maxReceive} into the buffer; returns the amount accepted. */
    public int receive(UUID owner, int maxReceive, boolean simulate) {
        int current = getEnergy(owner);
        int accepted = Math.min(MAX_ENERGY - current, Math.max(0, maxReceive));
        if (!simulate && accepted > 0) {
            coilEntries.put(owner, current + accepted);
            setDirty();
        }
        return accepted;
    }

    /** Extracts up to {@code maxExtract} from the buffer; returns the amount removed. */
    public int extract(UUID owner, int maxExtract, boolean simulate) {
        int current = getEnergy(owner);
        int removed = Math.min(current, Math.max(0, maxExtract));
        if (!simulate && removed > 0) {
            coilEntries.put(owner, current - removed);
            setDirty();
        }
        return removed;
    }
}
