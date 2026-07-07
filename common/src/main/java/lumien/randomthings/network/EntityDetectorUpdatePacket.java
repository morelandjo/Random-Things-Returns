package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.EntityDetectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.EntityDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Serverbound: Entity Detector GUI actions.
 *
 * <p>{@code action}: 0 cycle filter · 1/2/3 set range X/Y/Z (value = new range) ·
 * 4 toggle invert · 5 toggle strong output.</p>
 */
public record EntityDetectorUpdatePacket(BlockPos pos, int action, int value) implements RTPacket {

    public static final int ACTION_CYCLE_FILTER = 0;
    public static final int ACTION_SET_RANGE_X = 1;
    public static final int ACTION_SET_RANGE_Y = 2;
    public static final int ACTION_SET_RANGE_Z = 3;
    public static final int ACTION_TOGGLE_INVERT = 4;
    public static final int ACTION_TOGGLE_STRONG = 5;

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "entity_detector_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(action);
        buf.writeVarInt(value);
    }

    public static EntityDetectorUpdatePacket decode(FriendlyByteBuf buf) {
        return new EntityDetectorUpdatePacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof EntityDetectorMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof EntityDetectorBlockEntity detector)) return;

            switch (action) {
                case ACTION_CYCLE_FILTER -> detector.cycleFilter();
                case ACTION_SET_RANGE_X -> detector.setRange(0, value);
                case ACTION_SET_RANGE_Y -> detector.setRange(1, value);
                case ACTION_SET_RANGE_Z -> detector.setRange(2, value);
                case ACTION_TOGGLE_INVERT -> detector.toggleInvert();
                case ACTION_TOGGLE_STRONG -> detector.toggleStrongOutput();
            }
        });
    }
}
