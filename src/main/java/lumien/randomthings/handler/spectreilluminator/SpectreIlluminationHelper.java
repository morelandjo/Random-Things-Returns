package lumien.randomthings.handler.spectreilluminator;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class SpectreIlluminationHelper {
    /**
     * Forces light updates for every block in the chunk and rebuilds client rendering
     */
    public static void lightUpdateChunk(Level level, ChunkPos chunkPos) {
        if (!level.isLoaded(chunkPos.getWorldPosition())) {
            return;
        }

        // Update edges too (+-1)
        int minX = chunkPos.getMinBlockX() - 1;
        int maxX = chunkPos.getMaxBlockX() + 1;
        int minZ = chunkPos.getMinBlockZ() - 1;
        int maxZ = chunkPos.getMaxBlockZ() + 1;
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        // Force light update for every block in the chunk
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            level.getChunkSource().getLightEngine().checkBlock(pos.immutable());
        }

        // On the client side, force chunk sections to rebuild
        if (level.isClientSide) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.levelRenderer != null) {
                // Rebuild all sections in the chunk
                int minSection = level.getMinSection();
                int maxSection = level.getMaxSection();

                for (int sectionY = minSection; sectionY < maxSection; sectionY++) {
                    SectionPos sectionPos = SectionPos.of(chunkPos, sectionY);
                    mc.levelRenderer.setSectionDirty(sectionPos.x(), sectionPos.y(), sectionPos.z());
                }
            }
        }
    }
}
