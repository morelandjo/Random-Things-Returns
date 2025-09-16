package lumien.randomthings.network;

import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import lumien.randomthings.network.messages.MessageNotification;
import lumien.randomthings.network.messages.VisualEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import lumien.randomthings.client.vfx.EFFECT;

public class RTPacketHandler {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModConstants.MOD_ID);
        
        registrar.playToServer(ContainerSignalMessage.TYPE, ContainerSignalMessage.STREAM_CODEC, 
            (message, context) -> message.handle(context));
        registrar.playToClient(VisualEffectMessage.TYPE, VisualEffectMessage.STREAM_CODEC, 
            (message, context) -> message.handle(context));
        registrar.playToServer(AnalogEmitterPacket.TYPE, AnalogEmitterPacket.STREAM_CODEC, 
            (message, context) -> AnalogEmitterPacket.handle(message, context));
        registrar.playToServer(ChatDetectorUpdatePacket.TYPE, ChatDetectorUpdatePacket.STREAM_CODEC, 
            (message, context) -> ChatDetectorUpdatePacket.handle(message, context));
        registrar.playToClient(EclipsedClockAnimationPacket.TYPE, EclipsedClockAnimationPacket.STREAM_CODEC, 
            (message, context) -> EclipsedClockAnimationPacket.handle(message, context));
        // registrar.playToServer(OnlineDetectorUpdatePacket.TYPE, OnlineDetectorUpdatePacket.STREAM_CODEC, 
        //     (message, context) -> OnlineDetectorUpdatePacket.handle(message, context));
        registrar.playToServer(GlobalChatDetectorUpdatePacket.TYPE, GlobalChatDetectorUpdatePacket.STREAM_CODEC, 
            (message, context) -> GlobalChatDetectorUpdatePacket.handle(message, context));
        registrar.playToServer(IronDropperPacket.TYPE, IronDropperPacket.STREAM_CODEC, 
            (message, context) -> IronDropperPacket.handle(message, context));
        registrar.playToServer(IgniterPacket.TYPE, IgniterPacket.STREAM_CODEC, 
            (message, context) -> IgniterPacket.handle(message, context));
        registrar.playToServer(NotificationInterfaceUpdatePacket.TYPE, NotificationInterfaceUpdatePacket.STREAM_CODEC, 
            (message, context) -> NotificationInterfaceUpdatePacket.handle(message, context));
        registrar.playToClient(MessageNotification.TYPE, MessageNotification.STREAM_CODEC, 
            (message, context) -> MessageNotification.handle(message, context));
        registrar.playToServer(ChunkAnalyzerPacket.TYPE, ChunkAnalyzerPacket.STREAM_CODEC, 
            (message, context) -> ChunkAnalyzerPacket.handle(message, context));
        registrar.playToClient(ChunkAnalyzerResultPacket.TYPE, ChunkAnalyzerResultPacket.STREAM_CODEC,
            (message, context) -> ChunkAnalyzerResultPacket.handle(message, context));
        registrar.playToServer(EnderLetterUpdatePacket.TYPE, EnderLetterUpdatePacket.STREAM_CODEC,
            (message, context) -> EnderLetterUpdatePacket.handle(message, context));
    }
    
    public static void sendToTracking(Level level, BlockPos pos, VisualEffectMessage message) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunkAt(pos).getPos(), message);
        }
    }
    
    public static void sendToPlayer(MessageNotification message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
    
    public static void sendToServer(ChunkAnalyzerPacket message) {
        PacketDistributor.sendToServer(message);
    }
    
    public static void sendToPlayer(ChunkAnalyzerResultPacket message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendToServer(EnderLetterUpdatePacket message) {
        PacketDistributor.sendToServer(message);
    }
}