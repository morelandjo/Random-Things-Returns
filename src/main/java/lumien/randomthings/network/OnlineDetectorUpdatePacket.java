package lumien.randomthings.network;

import lumien.randomthings.blockentity.OnlineDetectorBlockEntity;
import lumien.randomthings.menu.OnlineDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OnlineDetectorUpdatePacket(BlockPos pos, String username) implements CustomPacketPayload {
    public static final Type<OnlineDetectorUpdatePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "online_detector_update"));

    public static final StreamCodec<FriendlyByteBuf, OnlineDetectorUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OnlineDetectorUpdatePacket::pos,
        StreamCodec.of((buf, username) -> buf.writeUtf(username), buf -> buf.readUtf()), OnlineDetectorUpdatePacket::username,
        OnlineDetectorUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OnlineDetectorUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                if (player.containerMenu instanceof OnlineDetectorMenu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    if (blockEntity instanceof OnlineDetectorBlockEntity onlineDetector) {
                        onlineDetector.setUsername(packet.username);
                    }
                }
            }
        });
    }
}
