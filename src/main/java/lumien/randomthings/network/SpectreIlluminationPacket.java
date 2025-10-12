package lumien.randomthings.network;

import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpectreIlluminationPacket(String dimension, long chunkLong, boolean illuminated) implements CustomPacketPayload {
    public static final Type<SpectreIlluminationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "spectre_illumination"));

    public static final StreamCodec<FriendlyByteBuf, SpectreIlluminationPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of((buf, dimension) -> buf.writeUtf(dimension), buf -> buf.readUtf()), SpectreIlluminationPacket::dimension,
        StreamCodec.of((buf, chunkLong) -> buf.writeLong(chunkLong), buf -> buf.readLong()), SpectreIlluminationPacket::chunkLong,
        StreamCodec.of((buf, illuminated) -> buf.writeBoolean(illuminated), buf -> buf.readBoolean()), SpectreIlluminationPacket::illuminated,
        SpectreIlluminationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SpectreIlluminationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound() && context.player().level() != null) {
                // Check if we're in the correct dimension
                String currentDimension = context.player().level().dimension().location().toString();
                if (currentDimension.equals(packet.dimension)) {
                    SpectreIlluminationClientHandler.setIlluminated(packet.chunkLong, packet.illuminated);
                }
            }
        });
    }
}
