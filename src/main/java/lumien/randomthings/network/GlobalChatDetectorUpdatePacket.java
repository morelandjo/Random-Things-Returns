package lumien.randomthings.network;

import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.menu.GlobalChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GlobalChatDetectorUpdatePacket(BlockPos pos, String message, boolean consumeMessage) implements CustomPacketPayload {
    public static final Type<GlobalChatDetectorUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "global_chat_detector_update"));
    
    public static final StreamCodec<FriendlyByteBuf, GlobalChatDetectorUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, GlobalChatDetectorUpdatePacket::pos,
        StreamCodec.of((buf, message) -> buf.writeUtf(message), buf -> buf.readUtf()), GlobalChatDetectorUpdatePacket::message,
        StreamCodec.of((buf, consume) -> buf.writeBoolean(consume), buf -> buf.readBoolean()), GlobalChatDetectorUpdatePacket::consumeMessage,
        GlobalChatDetectorUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GlobalChatDetectorUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (player.containerMenu instanceof GlobalChatDetectorMenu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    
                    if (blockEntity instanceof GlobalChatDetectorBlockEntity globalChatDetector) {
                        globalChatDetector.setDetectionMessage(packet.message);
                        globalChatDetector.setConsumeMessage(packet.consumeMessage);
                        // Set owner when first configured
                        if (globalChatDetector.getOwnerUUID() == null) {
                            globalChatDetector.setOwnerUUID(player.getUUID());
                        }
                    }
                }
            }
        });
    }
}