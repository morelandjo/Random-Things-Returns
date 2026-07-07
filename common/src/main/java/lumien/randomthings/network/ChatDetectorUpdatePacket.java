package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.ChatDetectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.ChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record ChatDetectorUpdatePacket(BlockPos pos, String message, boolean consumeMessage) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "chat_detector_update");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(message);
        buf.writeBoolean(consumeMessage);
    }

    public static ChatDetectorUpdatePacket decode(FriendlyByteBuf buf) {
        return new ChatDetectorUpdatePacket(buf.readBlockPos(), buf.readUtf(), buf.readBoolean());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof ChatDetectorMenu)) return;
            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (blockEntity instanceof ChatDetectorBlockEntity chatDetector) {
                chatDetector.setDetectionMessage(message);
                chatDetector.setConsumeMessage(consumeMessage);
                if (chatDetector.getOwnerUUID() == null) {
                    chatDetector.setOwnerUUID(player.getUUID());
                }
            }
        });
    }
}
