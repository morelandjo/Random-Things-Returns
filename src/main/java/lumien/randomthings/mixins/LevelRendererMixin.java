package lumien.randomthings.mixins;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1000)
public class LevelRendererMixin {
    
    /**
     * Block rain rendering (falling rain lines) in Rain Shield protected areas
     */
    @Inject(
        method = "renderSnowAndRain",
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private void onRenderSnowAndRain(LightTexture lightTexture, float partialTicks, double camX, double camY, double camZ, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                Level level = mc.level;
                BlockPos playerPos = mc.player.blockPosition();
                
                // Check if player is in a rain shield protected area
                if (!RainShieldBlockEntity.shouldRain(level, playerPos)) {
                    ci.cancel(); // Block rain rendering
                }
            }
        } catch (Exception e) {
            // Fail silently to avoid crashes
        }
    }
}