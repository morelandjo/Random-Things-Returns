package lumien.randomthings.mixin.client;

import lumien.randomthings.client.SoundMuteHandler;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sound Dampeners / Sound Recorder: intercept every sound before it plays (cross-loader PlaySoundEvent). */
@Mixin(SoundEngine.class)
public class SoundEngineMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void randomthings$interceptSound(SoundInstance sound, CallbackInfo ci) {
        if (SoundMuteHandler.onPlaySound(sound)) {
            ci.cancel();
        }
    }
}
