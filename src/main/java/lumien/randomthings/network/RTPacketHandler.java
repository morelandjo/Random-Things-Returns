package lumien.randomthings.network;

import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.messages.ContainerSignalMessage;
import lumien.randomthings.network.messages.VisualEffectMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
    }
    
    public static void sendToTracking(Level level, BlockPos pos, VisualEffectMessage message) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunkAt(pos).getPos(), message);
        }
    }
}