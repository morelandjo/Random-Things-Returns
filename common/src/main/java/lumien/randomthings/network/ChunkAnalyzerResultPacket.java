package lumien.randomthings.network;

import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Clientbound: the finished chunk-scan result, shown in the Chunk Analyzer screen. */
public record ChunkAnalyzerResultPacket(ChunkAnalyzerResult result) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "chunk_analyzer_result");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        result.toBuffer(buf);
    }

    public static ChunkAnalyzerResultPacket decode(FriendlyByteBuf buf) {
        return new ChunkAnalyzerResultPacket(ChunkAnalyzerResult.fromBuffer(buf));
    }
}
