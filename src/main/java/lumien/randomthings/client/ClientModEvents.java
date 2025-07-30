package lumien.randomthings.client;

import lumien.randomthings.client.renderer.DiviningRodRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class ClientModEvents {
    
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            
            DiviningRodRenderer.get().render(
                event.getPoseStack(), 
                bufferSource, 
                event.getPartialTick().getGameTimeDeltaPartialTick(false)
            );
            
            // Explicitly flush the buffer source to ensure rendering
            bufferSource.endBatch();
        }
    }
    
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        DiviningRodRenderer.get().tick();
    }
}