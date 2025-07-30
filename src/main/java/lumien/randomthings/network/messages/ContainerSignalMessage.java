package lumien.randomthings.network.messages;

import lumien.randomthings.menu.ISignalContainer;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ContainerSignalMessage(int id, byte[] data) implements IRTMessage {
    public static final Type<ContainerSignalMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "container_signal"));
    
    public static final StreamCodec<FriendlyByteBuf, ContainerSignalMessage> STREAM_CODEC = StreamCodec.composite(
        net.minecraft.network.codec.ByteBufCodecs.VAR_INT, ContainerSignalMessage::id,
        net.minecraft.network.codec.ByteBufCodecs.BYTE_ARRAY, ContainerSignalMessage::data,
        ContainerSignalMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        handle(this, context);
    }

    public static void handle(ContainerSignalMessage msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                AbstractContainerMenu container = serverPlayer.containerMenu;

                if (container instanceof ISignalContainer signalContainer) {
                    FriendlyByteBuf dataBuffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.copiedBuffer(msg.data));
                    signalContainer.handle(msg.id, dataBuffer);
                }
            }
        });
    }
}