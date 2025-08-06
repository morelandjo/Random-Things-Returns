package lumien.randomthings.network;

import lumien.randomthings.blockentity.AnalogEmitterBlockEntity;
import lumien.randomthings.menu.AnalogEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnalogEmitterPacket(BlockPos pos, int level) implements CustomPacketPayload {
    public static final Type<AnalogEmitterPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "analog_emitter"));
    
    public static final StreamCodec<FriendlyByteBuf, AnalogEmitterPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AnalogEmitterPacket::pos,
        StreamCodec.of((buf, level) -> buf.writeInt(level), buf -> buf.readInt()), AnalogEmitterPacket::level,
        AnalogEmitterPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AnalogEmitterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (packet.level > 0 && packet.level < 16) {
                    if (player.containerMenu instanceof AnalogEmitterMenu) {
                        BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                        
                        if (blockEntity instanceof AnalogEmitterBlockEntity analogEmitter) {
                            analogEmitter.setEmitLevel(packet.level);
                        }
                    }
                }
            }
        });
    }
}