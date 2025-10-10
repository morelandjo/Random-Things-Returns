package lumien.randomthings.network;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundRecorderItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from client to server when a sound is played while the player has a Sound Recorder in their inventory.
 * Server validates and records the sound to the appropriate recorder item.
 */
public record SoundPlayedPacket(ResourceLocation soundName, int recorderSlot) implements CustomPacketPayload {
    public static final Type<SoundPlayedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "sound_played"));

    public static final StreamCodec<FriendlyByteBuf, SoundPlayedPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, SoundPlayedPacket::soundName,
        StreamCodec.of((buf, slot) -> buf.writeInt(slot), FriendlyByteBuf::readInt), SoundPlayedPacket::recorderSlot,
        SoundPlayedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SoundPlayedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();

                // Validate slot is within inventory bounds
                if (packet.recorderSlot >= 0 && packet.recorderSlot < player.getInventory().getContainerSize()) {
                    ItemStack recorderStack = player.getInventory().getItem(packet.recorderSlot);

                    // Validate it's actually a Sound Recorder item
                    if (!recorderStack.isEmpty() && recorderStack.is(ModItems.SOUND_RECORDER.get())) {
                        SoundRecorderItem.recordSound(recorderStack, packet.soundName);
                    }
                }
            }
        });
    }
}
