package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central network registration + send helpers, backed by Architectury {@link NetworkManager}.
 *
 * <p>{@link #register()} is called once from common init and wires every receiver. Client→server
 * receivers run on the server; server→client receivers run on the client (their handlers must keep
 * {@code Minecraft.getInstance()} access inside client-only classes).</p>
 */
public final class RTNetwork {

    private RTNetwork() {
    }

    public static void register() {
        // Client -> Server
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, OnlineDetectorUpdatePacket.ID, (buf, ctx) ->
            OnlineDetectorUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, EntityDetectorUpdatePacket.ID, (buf, ctx) ->
            EntityDetectorUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ChatDetectorUpdatePacket.ID, (buf, ctx) ->
            ChatDetectorUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, GlobalChatDetectorUpdatePacket.ID, (buf, ctx) ->
            GlobalChatDetectorUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AdvancedItemCollectorPacket.ID, (buf, ctx) ->
            AdvancedItemCollectorPacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AnalogEmitterUpdatePacket.ID, (buf, ctx) ->
            AnalogEmitterUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AdvancedRedstoneTorchUpdatePacket.ID, (buf, ctx) ->
            AdvancedRedstoneTorchUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, AdvancedRedstoneRepeaterUpdatePacket.ID, (buf, ctx) ->
            AdvancedRedstoneRepeaterUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, IgniterUpdatePacket.ID, (buf, ctx) ->
            IgniterUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, IronDropperUpdatePacket.ID, (buf, ctx) ->
            IronDropperUpdatePacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SoundPlayedPacket.ID, (buf, ctx) ->
            SoundPlayedPacket.decode(buf).handle(ctx));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, SoundRecorderWritePacket.ID, (buf, ctx) ->
            SoundRecorderWritePacket.decode(buf).handle(ctx));

        // Server -> Client receivers are registered here (or in a client-only registrar) as features
        // that push data to clients are ported.
    }

    private static FriendlyByteBuf buffer(RTPacket packet) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.encode(buf);
        return buf;
    }

    /** Send a packet to the server (call from client). */
    public static void sendToServer(RTPacket packet) {
        NetworkManager.sendToServer(packet.id(), buffer(packet));
    }

    /** Send a packet to a specific player (call from server). */
    public static void sendToPlayer(ServerPlayer player, RTPacket packet) {
        NetworkManager.sendToPlayer(player, packet.id(), buffer(packet));
    }
}
