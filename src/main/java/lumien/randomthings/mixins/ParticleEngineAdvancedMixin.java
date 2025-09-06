package lumien.randomthings.mixins;

import lumien.randomthings.blockentity.RainShieldBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ParticleEngine.class, priority = 1200)
public class ParticleEngineAdvancedMixin {
    
    /**
     * Intercept the main add() method that all particles go through
     * Only block rain-related particles, allow others to render normally
     */
    @Inject(
        method = "add(Lnet/minecraft/client/particle/Particle;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private void onAddParticle(Particle particle, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && particle != null) {
                // Get particle position
                double x = particle.x;
                double y = particle.y;
                double z = particle.z;
                BlockPos pos = BlockPos.containing(x, y, z);
                
                // Check if this position is protected by a Rain Shield
                if (!RainShieldBlockEntity.shouldRain(mc.level, pos)) {
                    // Only block rain-related particles by checking particle class names
                    String particleType = particle.getClass().getSimpleName();
                    boolean isRainRelated = particleType.toLowerCase().contains("rain") || 
                                          particleType.toLowerCase().contains("drip") ||
                                          particleType.toLowerCase().contains("splash") ||
                                          particleType.toLowerCase().contains("water");
                    
                    if (isRainRelated) {
                        ci.cancel(); // Block only rain-related particles
                    }
                }
            }
        } catch (Exception e) {
            // Fail silently to avoid crashes
        }
    }
    
    /**
     * Also intercept createParticle method as an alternative entry point
     */
    @Inject(
        method = "createParticle",
        at = @At("HEAD"),
        cancellable = true,
        require = 0,
        remap = false
    )
    private void onCreateParticle(ParticleOptions particleData, double x, double y, double z, 
                                  double xSpeed, double ySpeed, double zSpeed, CallbackInfoReturnable<Particle> cir) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                BlockPos pos = BlockPos.containing(x, y, z);
                
                // Check if this position is protected by a Rain Shield
                if (!RainShieldBlockEntity.shouldRain(mc.level, pos)) {
                    // Only block specific rain-related particle types
                    boolean isRainParticle = particleData.getType() == ParticleTypes.RAIN ||
                                           particleData.getType() == ParticleTypes.SPLASH ||
                                           particleData.getType() == ParticleTypes.DRIPPING_WATER ||
                                           particleData.getType() == ParticleTypes.FALLING_WATER;
                    
                    if (isRainParticle) {
                        cir.setReturnValue(null); // Block only rain particle creation
                    }
                }
            }
        } catch (Exception e) {
            // Fail silently to avoid crashes
        }
    }
}