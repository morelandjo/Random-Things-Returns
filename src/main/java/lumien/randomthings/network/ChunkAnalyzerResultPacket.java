package lumien.randomthings.network;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Player player = Minecraft.getInstance().player;
                if (player != null) {
                    ItemStack heldItem = player.getMainHandItem();
                    
                    if (heldItem.getItem() instanceof ChunkAnalyzerItem && !heldItem.isEmpty()) {
                        ChunkAnalyzerItem.setResults(heldItem, packet.result);
                    }
                }
            }
        });
    }
}