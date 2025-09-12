package lumien.randomthings.network;

import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EclipsedClockAnimationPacket(int entityId) implements CustomPacketPayload {
    public static final Type<EclipsedClockAnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "eclipsed_clock_animation"));
    
    public static final StreamCodec<FriendlyByteBuf, EclipsedClockAnimationPacket> STREAM_CODEC = StreamCodec.composite(
        StreamCodec.of((buf, entityId) -> buf.writeInt(entityId), buf -> buf.readInt()), EclipsedClockAnimationPacket::entityId,
        EclipsedClockAnimationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EclipsedClockAnimationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound() && context.player().level() != null) {
                Entity entity = context.player().level().getEntity(packet.entityId);
                
                if (entity instanceof EclipsedClockEntity clockEntity) {
                    clockEntity.triggerAnimation();
                }
            }
        });
    }
}