package lumien.randomthings.network;

import lumien.randomthings.blockentity.NotificationInterfaceBlockEntity;
import lumien.randomthings.menu.NotificationInterfaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NotificationInterfaceUpdatePacket(BlockPos pos, String title, String description) implements CustomPacketPayload {
    public static final Type<NotificationInterfaceUpdatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "notification_interface_update"));
    
    public static final StreamCodec<FriendlyByteBuf, NotificationInterfaceUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, NotificationInterfaceUpdatePacket::pos,
        StreamCodec.of((buf, title) -> buf.writeUtf(title), buf -> buf.readUtf()), NotificationInterfaceUpdatePacket::title,
        StreamCodec.of((buf, description) -> buf.writeUtf(description), buf -> buf.readUtf()), NotificationInterfaceUpdatePacket::description,
        NotificationInterfaceUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(NotificationInterfaceUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (player.containerMenu instanceof NotificationInterfaceMenu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    
                    if (blockEntity instanceof NotificationInterfaceBlockEntity notificationInterface) {
                        notificationInterface.setData(packet.title, packet.description);
                    }
                }
            }
        });
    }
}