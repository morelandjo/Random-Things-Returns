package lumien.randomthings.client.handler;

import lumien.randomthings.block.LightRedirectorBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

/**
 * Event-based handler for Light Redirector visual effects using NeoForge render events.
 * NOTE: This class is currently empty as the texture swapping approach has been removed.
 */
// @EventBusSubscriber(value = Dist.CLIENT, modid = "randomthings") // Commented out - no event handlers
public class LightRedirectorRenderHandler {

    // Event handlers removed - no longer using texture swapping approach
    
}