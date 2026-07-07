package lumien.randomthings.client;

import lumien.randomthings.util.ChunkAnalyzerResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Holds the latest chunk-scan result received from the server, for the Chunk Analyzer screen. */
@Environment(EnvType.CLIENT)
public final class ChunkAnalyzerClientData {
    private static ChunkAnalyzerResult latestResult = ChunkAnalyzerResult.empty();

    private ChunkAnalyzerClientData() {
    }

    public static void setLatestResult(ChunkAnalyzerResult result) {
        latestResult = result;
    }

    public static ChunkAnalyzerResult getLatestResult() {
        return latestResult;
    }
}
