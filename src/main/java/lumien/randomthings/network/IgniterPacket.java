package lumien.randomthings.network;

import lumien.randomthings.blockentity.IgniterBlockEntity;
import lumien.randomthings.menu.IgniterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IgniterPacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<IgniterPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "igniter"));
    
    public static final StreamCodec<FriendlyByteBuf, IgniterPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, IgniterPacket::pos,
        IgniterPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IgniterPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (player.containerMenu instanceof IgniterMenu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    
                    if (blockEntity instanceof IgniterBlockEntity igniter) {
                        igniter.rotateMode();
                    }
                }
            }
        });
    }
}