package lumien.randomthings.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * A Random Things network packet. Each packet knows its channel {@link #id()} and how to
 * {@link #encode(FriendlyByteBuf)} itself; a static {@code decode(FriendlyByteBuf)} method rebuilds
 * it on the receiving side. This is the 1.20.1 replacement for 1.21.1 {@code CustomPacketPayload} +
 * {@code StreamCodec}.
 */
public interface RTPacket {

    ResourceLocation id();

    void encode(FriendlyByteBuf buf);
}
