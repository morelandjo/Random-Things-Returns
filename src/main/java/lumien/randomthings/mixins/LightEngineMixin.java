package lumien.randomthings.mixins;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.handler.spectreilluminator.SpectreIlluminationClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept brightness queries in LightEngine
 * This is the actual implementation that returns light values for both sky and block light
 * Used by the rendering system via BlockAndTintGetter.getBrightness() -> LayerLightEventListener.getLightValue()
 */
@Mixin(LightEngine.class)
public abstract class LightEngineMixin {

    @Shadow
    @Final
    protected net.minecraft.world.level.chunk.LightChunkGetter chunkSource;

    /**
     * Intercept getLightValue which is the actual method that returns light values
     */
    @Inject(
            method = "getLightValue(Lnet/minecraft/core/BlockPos;)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void randomthings$getLightValue(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        // Get the level from the chunk source
        Level level = (Level) chunkSource.getLevel();

        boolean isIlluminated;

        // Use the appropriate checker based on side
        if (level.isClientSide) {
            // On client, use the client handler which is synced via packets
            isIlluminated = SpectreIlluminationClientHandler.isIlluminated(pos);
        } else {
            // On server, use the entity's static map
            isIlluminated = SpectreIlluminatorEntity.isChunkIlluminated(pos, level);
        }

        if (isIlluminated) {
            // Return max brightness (15)
            cir.setReturnValue(15);
        }
    }
}
