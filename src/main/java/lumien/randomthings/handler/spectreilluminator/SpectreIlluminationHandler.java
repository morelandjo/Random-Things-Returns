package lumien.randomthings.handler.spectreilluminator;

import lumien.randomthings.network.SpectreIlluminationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;

public class SpectreIlluminationHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_spectre_illumination";

    private final Set<Long> illuminatedChunks = new HashSet<>();

    public SpectreIlluminationHandler() {
    }

    public SpectreIlluminationHandler(CompoundTag tag, HolderLookup.Provider registries) {
        load(tag, registries);
    }

    public static SpectreIlluminationHandler get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(
                new Factory<>(
                    SpectreIlluminationHandler::new,
                    (tag, registries) -> new SpectreIlluminationHandler(tag, registries),
                    null
                ),
                DATA_NAME
            );
        }
        // Client-side should not access this
        throw new IllegalStateException("Cannot access SpectreIlluminationHandler on client side!");
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        illuminatedChunks.clear();
        ListTag list = tag.getList("illuminatedChunks", Tag.TAG_LONG);
        for (int i = 0; i < list.size(); i++) {
            illuminatedChunks.add(((LongTag) list.get(i)).getAsLong());
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Long chunkLong : illuminatedChunks) {
            list.add(LongTag.valueOf(chunkLong));
        }
        tag.put("illuminatedChunks", list);
        return tag;
    }

    public void startWatching(LevelChunk chunk, ServerPlayer player) {
        long chunkLong = chunk.getPos().toLong();
        if (illuminatedChunks.contains(chunkLong)) {
            // Send packet to client to notify them this chunk is illuminated
            PacketDistributor.sendToPlayer(player, new SpectreIlluminationPacket(
                chunk.getLevel().dimension().location().toString(),
                chunkLong,
                true
            ));
        }
    }

    public boolean isIlluminated(BlockPos pos) {
        return illuminatedChunks.contains(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public void toggleChunk(Level level, BlockPos pos) {
        long chunkLong = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        ChunkPos chunkPos = new ChunkPos(pos);

        boolean newValue;
        if (illuminatedChunks.contains(chunkLong)) {
            illuminatedChunks.remove(chunkLong);
            newValue = false;
        } else {
            illuminatedChunks.add(chunkLong);
            newValue = true;
        }

        System.out.println("[SpectreIlluminator] Server: " + (newValue ? "Added" : "Removed") + " lighting for chunk " + chunkPos);

        // Force light updates for every block in the chunk
        SpectreIlluminationHelper.lightUpdateChunk(level, chunkPos);

        // Send packet to all players tracking this chunk
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos,
                new SpectreIlluminationPacket(
                    level.dimension().location().toString(),
                    chunkLong,
                    newValue
                )
            );
        }

        // Mark as dirty so it saves
        this.setDirty();
    }
}
