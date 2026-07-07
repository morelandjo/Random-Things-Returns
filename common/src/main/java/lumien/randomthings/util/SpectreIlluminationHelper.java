package lumien.randomthings.util;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/** Forces a client-side re-render of an illuminated chunk so the brightness change shows immediately. */
public final class SpectreIlluminationHelper {

    private SpectreIlluminationHelper() {
    }

    public static void lightUpdateChunk(Level level, ChunkPos chunkPos) {
        if (level.isClientSide) {
            // Guarded: only ever reached client-side, so the client-only class is never loaded on a server.
            markChunkForRerender(level, chunkPos);
        }
    }

    private static void markChunkForRerender(Level level, ChunkPos chunkPos) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.levelRenderer != null) {
            mc.levelRenderer.setBlocksDirty(
                chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                chunkPos.getMaxBlockX(), level.getMaxBuildHeight(), chunkPos.getMaxBlockZ());
        }
    }
}
