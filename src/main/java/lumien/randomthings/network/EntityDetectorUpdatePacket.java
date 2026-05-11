package lumien.randomthings.network;

import lumien.randomthings.blockentity.EntityDetectorBlockEntity;
import lumien.randomthings.menu.EntityDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Servrbound payload for Entity Detector GUI actions.
 *
 * <p>{@code action} encodings:
 * <ul>
 *   <li>0 — cycle filter</li>
 *   <li>1 — set rangeX (value carries the new range, 0..MAX_RANGE)</li>
 *   <li>2 — set rangeY</li>
 *   <li>3 — set rangeZ</li>
 *   <li>4 — toggle invert</li>
 *   <li>5 — toggle strong output</li>
 * </ul>
 */
public record EntityDetectorUpdatePacket(BlockPos pos, int action, int value) implements CustomPacketPayload {

    public static final int ACTION_CYCLE_FILTER = 0;
    public static final int ACTION_SET_RANGE_X = 1;
    public static final int ACTION_SET_RANGE_Y = 2;
    public static final int ACTION_SET_RANGE_Z = 3;
    public static final int ACTION_TOGGLE_INVERT = 4;
    public static final int ACTION_TOGGLE_STRONG = 5;

    public static final Type<EntityDetectorUpdatePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("randomthings", "entity_detector_update"));

    public static final StreamCodec<FriendlyByteBuf, EntityDetectorUpdatePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, EntityDetectorUpdatePacket::pos,
        StreamCodec.of((buf, v) -> buf.writeVarInt(v), FriendlyByteBuf::readVarInt), EntityDetectorUpdatePacket::action,
        StreamCodec.of((buf, v) -> buf.writeVarInt(v), FriendlyByteBuf::readVarInt), EntityDetectorUpdatePacket::value,
        EntityDetectorUpdatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EntityDetectorUpdatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().isServerbound()) return;
            ServerPlayer player = (ServerPlayer) context.player();
            if (!(player.containerMenu instanceof EntityDetectorMenu)) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof EntityDetectorBlockEntity detector)) return;

            switch (packet.action) {
                case ACTION_CYCLE_FILTER -> detector.cycleFilter();
                case ACTION_SET_RANGE_X -> detector.setRange(0, packet.value);
                case ACTION_SET_RANGE_Y -> detector.setRange(1, packet.value);
                case ACTION_SET_RANGE_Z -> detector.setRange(2, packet.value);
                case ACTION_TOGGLE_INVERT -> detector.toggleInvert();
                case ACTION_TOGGLE_STRONG -> detector.toggleStrongOutput();
            }
        });
    }
}
