package lumien.randomthings.network;

import dev.architectury.networking.NetworkManager;
import lumien.randomthings.blockentity.GlobalChatDetectorBlockEntity;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.menu.GlobalChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

public record GlobalChatDetectorUpdatePacket(BlockPos pos, String message, boolean consumeMessage) implements RTPacket {

    public static final ResourceLocation ID = new ResourceLocation(ModConstants.MOD_ID, "global_chat_detector_update");

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

    public static GlobalChatDetectorUpdatePacket decode(FriendlyByteBuf buf) {
        return new GlobalChatDetectorUpdatePacket(buf.readBlockPos(), buf.readUtf(), buf.readBoolean());
    }

    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof GlobalChatDetectorMenu)) return;
            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (blockEntity instanceof GlobalChatDetectorBlockEntity globalChatDetector) {
                globalChatDetector.setDetectionMessage(message);
                globalChatDetector.setConsumeMessage(consumeMessage);
                if (globalChatDetector.getOwnerUUID() == null) {
                    globalChatDetector.setOwnerUUID(player.getUUID());
                }
            }
        });
    }
}
