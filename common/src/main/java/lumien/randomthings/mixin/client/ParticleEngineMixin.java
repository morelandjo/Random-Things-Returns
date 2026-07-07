package lumien.randomthings.mixin.client;

import lumien.randomthings.client.MagicHoodClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Magic Hood: suppress potion-effect particles around players wearing the hood. */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

    @Inject(
        method = "createParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true)
    private void randomthings$magicHoodHidesPotionParticles(ParticleOptions options, double x, double y, double z,
                                                            double xd, double yd, double zd,
                                                            CallbackInfoReturnable<Particle> cir) {
        if (options.getType() != ParticleTypes.ENTITY_EFFECT && options.getType() != ParticleTypes.AMBIENT_ENTITY_EFFECT) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.players().stream().anyMatch(player ->
                MagicHoodClientHelper.isWearingMagicHood(player) && player.distanceToSqr(x, y, z) < 4.0)) {
            cir.setReturnValue(null);
        }
    }
}
