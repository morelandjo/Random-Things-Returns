package lumien.randomthings.network.messages;

import lumien.randomthings.client.vfx.EFFECT;
import lumien.randomthings.client.vfx.VFXHandler;
import lumien.randomthings.client.vfx.VisualEffect;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VisualEffectMessage(EFFECT effectType, byte[] effectData) implements IRTMessage {
    public static final Type<VisualEffectMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "visual_effect"));
    
    public static final StreamCodec<FriendlyByteBuf, VisualEffectMessage> STREAM_CODEC = StreamCodec.of(
        (buffer, msg) -> {
            buffer.writeInt(msg.effectType.ordinal());
            buffer.writeByteArray(msg.effectData);
        },
        (buffer) -> {
            EFFECT effectType = EFFECT.values()[buffer.readInt()];
            byte[] data = buffer.readByteArray();
            return new VisualEffectMessage(effectType, data);
        }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        handle(this, context);
    }

    public static void handle(VisualEffectMessage msg, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().isClientSide) {
                try {
                    VisualEffect effect = msg.effectType.getEffectClass().getDeclaredConstructor().newInstance();
                    FriendlyByteBuf dataBuffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.copiedBuffer(msg.effectData));
                    effect.readData(dataBuffer);
                    VFXHandler.INSTANCE.addEffect(effect);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}