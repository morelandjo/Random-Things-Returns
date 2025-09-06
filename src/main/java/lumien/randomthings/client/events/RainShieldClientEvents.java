package lumien.randomthings.client.events;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;

@OnlyIn(Dist.CLIENT)
public class RainShieldClientEvents {
    
    
    /**
     * Block rain sounds in Rain Shield protected areas
     */
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (event.getSound() != null) {
            String soundName = event.getSound().getLocation().toString();
            
            // Check if it's a rain/weather sound
            if (soundName.contains("rain") || soundName.contains("weather")) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null && mc.player != null) {
                    BlockPos playerPos = mc.player.blockPosition();
                    
                    // Check if player is in a rain shield protected area  
                    if (!RainShieldBlockEntity.shouldRain(mc.level, playerPos)) {
                        // For now, we'll rely on the mixins for complete rain blocking
                        // Sound cancellation may need a different approach in NeoForge 1.21.1
                    }
                }
            }
        }
    }
    
    /**
     * Client tick handler - currently unused but kept for future features
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // Currently no client tick logic needed since we removed caching
    }
}