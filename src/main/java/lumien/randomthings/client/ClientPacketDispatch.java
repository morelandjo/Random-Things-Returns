package lumien.randomthings.client;

import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationClientHandler;
import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.network.ChunkAnalyzerResultPacket;
import lumien.randomthings.network.SpectreIlluminationPacket;
import lumien.randomthings.network.messages.VisualEffectMessage;
import lumien.randomthings.client.vfx.VFXHandler;
import lumien.randomthings.client.vfx.VisualEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only entry points for packet handlers. Anything that touches {@code Minecraft},
 * {@code LocalPlayer}, or client-side renderers must live here so the bytecode of the
 * common network classes stays free of client-typed constant pool entries — otherwise
 * NeoForge's RuntimeDistCleaner aborts class loading on the dedicated server.
 */
public final class ClientPacketDispatch {
    private ClientPacketDispatch() {}

    public static void handleChunkAnalyzerResult(ChunkAnalyzerResultPacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ChunkAnalyzerItem && !held.isEmpty()) {
            ChunkAnalyzerItem.setResults(held, packet.result());
        }
    }

    public static void handleSpectreIllumination(SpectreIlluminationPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        String currentDimension = mc.level.dimension().location().toString();
        if (currentDimension.equals(packet.dimension())) {
            SpectreIlluminationClientHandler.setIlluminated(packet.chunkLong(), packet.illuminated());
        }
    }

    public static void handleVisualEffect(VisualEffectMessage msg) {
        try {
            VisualEffect effect = msg.effectType().getEffectClass().getDeclaredConstructor().newInstance();
            FriendlyByteBuf dataBuffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.copiedBuffer(msg.effectData()));
            effect.readData(dataBuffer);
            VFXHandler.INSTANCE.addEffect(effect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
