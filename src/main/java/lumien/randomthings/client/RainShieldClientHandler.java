package lumien.randomthings.client;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainShieldClientHandler {
    
    /**
     * Check if rain should be rendered at a specific position for client-side effects.
     * This can be called by mixins or other client-side code to determine if rain effects
     * should be shown in a protected area.
     */
    public static boolean shouldRenderRain(BlockPos pos) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                return RainShieldBlockEntity.shouldRain(mc.level, pos);
            }
        } catch (Exception e) {
            // If anything goes wrong, default to allowing rain
        }
        return true;
    }
    
    /**
     * Check if the player is currently in a rain shield protected area
     */
    public static boolean isPlayerProtectedFromRain() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                BlockPos playerPos = mc.player.blockPosition();
                return !RainShieldBlockEntity.shouldRain(mc.level, playerPos);
            }
        } catch (Exception e) {
            // If anything goes wrong, default to not protected
        }
        return false;
    }
    
    /**
     * Modify client level rain level for protected areas.
     * This is called from client tick events to temporarily reduce rain effects.
     */
    public static float getEffectiveRainLevel(ClientLevel level, float originalRainLevel) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                BlockPos playerPos = mc.player.blockPosition();
                
                // If player is in protected area, return 0 rain level
                if (!RainShieldBlockEntity.shouldRain(level, playerPos)) {
                    return 0.0f;
                }
            }
        } catch (Exception e) {
            // If anything goes wrong, return original level
        }
        return originalRainLevel;
    }
}