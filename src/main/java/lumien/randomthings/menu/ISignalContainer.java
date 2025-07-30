package lumien.randomthings.menu;

import java.util.function.Consumer;

import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

public interface ISignalContainer {
    public void handle(int id, FriendlyByteBuf data);

    public default void send(int id, Consumer<FriendlyByteBuf> consumer) {
        FriendlyByteBuf pb = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        consumer.accept(pb);
        byte[] data = new byte[pb.readableBytes()];
        pb.readBytes(data);
        PacketDistributor.sendToServer(new ContainerSignalMessage(id, data));
    }
}