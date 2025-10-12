package lumien.randomthings.handler.spectreilluminator;

import lumien.randomthings.util.WorldUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashSet;
import java.util.Set;

public class SpectreIlluminationClientHandler {
    private static final Set<Long> illuminatedChunks = new HashSet<>();

    public static boolean isIlluminated(BlockPos pos) {
        return illuminatedChunks.contains(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
    }

    public static void loadChunk(LevelChunk chunk) {
        // Remove chunk from illuminated set when it loads
        // The server will send a packet if it should be illuminated
        illuminatedChunks.remove(chunk.getPos().toLong());
    }

    public static void setIlluminated(long chunkLong, boolean illuminated) {
        if (illuminated) {
            illuminatedChunks.add(chunkLong);
        } else {
            illuminatedChunks.remove(chunkLong);
        }

        // Force light recalculation for the chunk
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            ChunkPos chunkPos = new ChunkPos(chunkLong);
            SpectreIlluminationHelper.lightUpdateChunk(minecraft.level, chunkPos);

            // Also force a full chunk re-render
            if (minecraft.levelRenderer != null) {
                minecraft.levelRenderer.setBlocksDirty(
                    chunkPos.getMinBlockX(), minecraft.level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                    chunkPos.getMaxBlockX(), minecraft.level.getMaxBuildHeight(), chunkPos.getMaxBlockZ()
                );
            }
        }
    }

    public static void clearAll() {
        illuminatedChunks.clear();
    }
}
