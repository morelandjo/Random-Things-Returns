package lumien.randomthings.network;

import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.menu.ChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChatDetectorUpdatePacket(BlockPos pos, String message, boolean consumeMessage) implements CustomPacketPayload {
    public static final Type<ChatDetectorUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "chat_detector_update"));
    
    public static final StreamCodec<FriendlyByteBuf, ChatDetectorUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, ChatDetectorUpdatePacket::pos,
        StreamCodec.of((buf, message) -> buf.writeUtf(message), buf -> buf.readUtf()), ChatDetectorUpdatePacket::message,
        StreamCodec.of((buf, consume) -> buf.writeBoolean(consume), buf -> buf.readBoolean()), ChatDetectorUpdatePacket::consumeMessage,
        ChatDetectorUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChatDetectorUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (player.containerMenu instanceof ChatDetectorMenu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    
                    if (blockEntity instanceof ChatDetectorBlockEntity chatDetector) {
                        chatDetector.setDetectionMessage(packet.message);
                        chatDetector.setConsumeMessage(packet.consumeMessage);
                        // Set owner when first configured
                        if (chatDetector.getOwnerUUID() == null) {
                            chatDetector.setOwnerUUID(player.getUUID());
                        }
                    }
                }
            }
        });
    }
}