package lumien.randomthings.network;

import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChunkAnalyzerResultPacket(ChunkAnalyzerResult result) implements CustomPacketPayload {
    public static final Type<ChunkAnalyzerResultPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "chunk_analyzer_result"));
    
    public static final StreamCodec<FriendlyByteBuf, ChunkAnalyzerResultPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of(
            (buf, result) -> buf.writeJsonWithCodec(ChunkAnalyzerResult.CODEC, result),
            buf -> buf.readJsonWithCodec(ChunkAnalyzerResult.CODEC)
        ), ChunkAnalyzerResultPacket::result,
        ChunkAnalyzerResultPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkAnalyzerResultPacket packet, IPayloadContext context) {
        if (!context.flow().isClientbound()) return;
        context.enqueueWork(() ->
            lumien.randomthings.client.ClientPacketDispatch.handleChunkAnalyzerResult(packet)
        );
    }
}