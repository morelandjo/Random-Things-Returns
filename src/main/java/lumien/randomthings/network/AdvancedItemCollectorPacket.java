package lumien.randomthings.network;

import lumien.randomthings.blockentity.AdvancedItemCollectorBlockEntity;
import lumien.randomthings.menu.AdvancedItemCollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AdvancedItemCollectorPacket(BlockPos pos, int axis, int value) implements CustomPacketPayload {

    public static final Type<AdvancedItemCollectorPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "advanced_item_collector_update"));

    public static final StreamCodec<FriendlyByteBuf, AdvancedItemCollectorPacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, AdvancedItemCollectorPacket::pos,
        StreamCodec.of((buf, v) -> buf.writeVarInt(v), FriendlyByteBuf::readVarInt), AdvancedItemCollectorPacket::axis,
        StreamCodec.of((buf, v) -> buf.writeVarInt(v), FriendlyByteBuf::readVarInt), AdvancedItemCollectorPacket::value,
        AdvancedItemCollectorPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AdvancedItemCollectorPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().isServerbound()) return;
            ServerPlayer player = (ServerPlayer) context.player();
            if (!(player.containerMenu instanceof AdvancedItemCollectorMenu)) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof AdvancedItemCollectorBlockEntity collector)) return;
            if (packet.axis < 0 || packet.axis > 2) return;
            collector.setRange(packet.axis, packet.value);
        });
    }
}
