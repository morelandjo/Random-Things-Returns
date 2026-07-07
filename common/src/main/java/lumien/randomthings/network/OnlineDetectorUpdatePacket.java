package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.OnlineDetectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.OnlineDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client→server: updates the watched username on an {@link OnlineDetectorBlockEntity}.
 */
public record OnlineDetectorUpdatePacket(BlockPos pos, String username) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "online_detector_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(username);
    }

    public static OnlineDetectorUpdatePacket decode(FriendlyByteBuf buf) {
        return new OnlineDetectorUpdatePacket(buf.readBlockPos(), buf.readUtf());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player
                && player.containerMenu instanceof OnlineDetectorMenu) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (blockEntity instanceof OnlineDetectorBlockEntity onlineDetector) {
                    onlineDetector.setUsername(username);
                }
            }
        });
    }
}
