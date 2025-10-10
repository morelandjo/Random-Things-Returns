package lumien.randomthings.network;

import lumien.randomthings.menu.SoundRecorderMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Sent from client to server when a player selects a sound in the Sound Recorder GUI.
 * Server validates and creates a filled Sound Pattern in the output slot.
 */
public record SoundSelectedPacket(ResourceLocation selectedSound) implements CustomPacketPayload {
    public static final Type<SoundSelectedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "sound_selected"));

    public static final StreamCodec<FriendlyByteBuf, SoundSelectedPacket> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, SoundSelectedPacket::selectedSound,
        SoundSelectedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SoundSelectedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();

                // Validate that the player has the Sound Recorder menu open
                if (player.containerMenu instanceof SoundRecorderMenu soundRecorderMenu && packet.selectedSound != null) {
                    soundRecorderMenu.outputSound(packet.selectedSound);
                }
            }
        });
    }
}
