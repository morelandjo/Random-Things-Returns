package lumien.randomthings.network;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.menu.ChunkAnalyzerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChunkAnalyzerPacket(Action action) implements CustomPacketPayload {
    public static final Type<ChunkAnalyzerPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "chunk_analyzer"));
    
    public static final StreamCodec<FriendlyByteBuf, ChunkAnalyzerPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of((buf, action) -> buf.writeEnum(action), buf -> buf.readEnum(Action.class)), ChunkAnalyzerPacket::action,
        ChunkAnalyzerPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChunkAnalyzerPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                // Validate that the player has the chunk analyzer open and is holding the item
                if (player.containerMenu instanceof ChunkAnalyzerMenu chunkAnalyzerMenu) {
                    ItemStack heldItem = player.getMainHandItem();
                    
                    if (heldItem.getItem() instanceof ChunkAnalyzerItem && !heldItem.isEmpty()) {
                        switch (packet.action) {
                            case START -> {
                                // Clear previous results and start scanning
                                ChunkAnalyzerItem.clearResults(heldItem);
                                chunkAnalyzerMenu.startScanning();
                            }
                            case STOP -> {
                                // Currently not used, but could be implemented for manual stopping
                            }
                        }
                    }
                }
            }
        });
    }

    public enum Action {
        START,
        STOP
    }
}