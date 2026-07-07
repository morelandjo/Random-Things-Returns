package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.AdvancedItemCollectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.AdvancedItemCollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Serverbound: set an Advanced Item Collector scan-range axis (0=X,1=Y,2=Z) to {@code value}. */
public record AdvancedItemCollectorPacket(BlockPos pos, int axis, int value) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "advanced_item_collector_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeVarInt(axis);
        buf.writeVarInt(value);
    }

    public static AdvancedItemCollectorPacket decode(FriendlyByteBuf buf) {
        return new AdvancedItemCollectorPacket(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof AdvancedItemCollectorMenu)) return;
            BlockEntity be = player.level().getBlockEntity(pos);
            if (!(be instanceof AdvancedItemCollectorBlockEntity collector)) return;
            if (axis < 0 || axis > 2) return;
            collector.setRange(axis, value);
        });
    }
}
