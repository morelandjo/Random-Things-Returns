package lumien.randomthings.mixin.client;

import lumien.randomthings.client.MagicHoodClientHelper;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Magic Hood: hide the nameplate of players wearing the hood. */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(
        method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;)Z",
        at = @At("HEAD"),
        cancellable = true)
    private void randomthings$magicHoodHidesName(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && MagicHoodClientHelper.isWearingMagicHood(player)) {
            cir.setReturnValue(false);
        }
    }
}
