package lumien.randomthings.network;

import lumien.randomthings.blockentity.IronDropperBlockEntity;
import lumien.randomthings.menu.IronDropperMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IronDropperPacket(BlockPos pos, int buttonId) implements CustomPacketPayload {
    public static final Type<IronDropperPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "iron_dropper"));
    
    public static final StreamCodec<FriendlyByteBuf, IronDropperPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, IronDropperPacket::pos,
        StreamCodec.of((buf, buttonId) -> buf.writeInt(buttonId), buf -> buf.readInt()), IronDropperPacket::buttonId,
        IronDropperPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IronDropperPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) context.player();
                
                if (player.containerMenu instanceof IronDropperMenu menu) {
                    BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                    
                    if (blockEntity instanceof IronDropperBlockEntity ironDropper) {
                        menu.handleButtonPress(packet.buttonId);
                    }
                }
            }
        });
    }
}