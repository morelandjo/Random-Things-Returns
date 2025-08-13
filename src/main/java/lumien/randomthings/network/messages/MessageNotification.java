package lumien.randomthings.network.messages;

import lumien.randomthings.client.notifications.NotificationToast;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MessageNotification(String title, String description, ItemStack icon) implements CustomPacketPayload {
    public static final Type<MessageNotification> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "notification"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageNotification> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of((buf, title) -> buf.writeUtf(title), buf -> buf.readUtf()), MessageNotification::title,
        StreamCodec.of((buf, description) -> buf.writeUtf(description), buf -> buf.readUtf()), MessageNotification::description,
        ItemStack.STREAM_CODEC, MessageNotification::icon,
        MessageNotification::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessageNotification packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                Minecraft.getInstance().getToasts().addToast(new NotificationToast(packet.title, packet.description, packet.icon));
            }
        });
    }
}