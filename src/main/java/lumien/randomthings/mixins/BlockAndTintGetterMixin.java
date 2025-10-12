package lumien.randomthings.mixins;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to override brightness for illuminated chunks on client level
 * Based on Irregular Implements implementation
 */
@Mixin(ClientLevel.class)
public abstract class BlockAndTintGetterMixin {

    private static boolean debugPrinted = false;

    @Inject(
            method = "getBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void randomthings$getBrightness(LightLayer lightLayer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        // Print once to confirm mixin is working
        if (!debugPrinted) {
            System.out.println("[SpectreIlluminator] Mixin getBrightness is ACTIVE and being called!");
            debugPrinted = true;
        }

        if (SpectreIlluminatorEntity.isChunkIlluminated(pos, (BlockAndTintGetter) this)) {
            System.out.println("[SpectreIlluminator] Mixin intercepted at " + pos + " - returning 15");
            cir.setReturnValue(15);
        }
    }

    @Inject(
            method = "getRawBrightness",
            at = @At("HEAD"),
            cancellable = true
    )
    private void randomthings$getRawBrightness(BlockPos pos, int ambientDarkening, CallbackInfoReturnable<Integer> cir) {
        if (SpectreIlluminatorEntity.isChunkIlluminated(pos, (BlockAndTintGetter) this)) {
            System.out.println("[SpectreIlluminator] Mixin getRawBrightness intercepted at " + pos + " - returning 15");
            cir.setReturnValue(15);
        }
    }
}

