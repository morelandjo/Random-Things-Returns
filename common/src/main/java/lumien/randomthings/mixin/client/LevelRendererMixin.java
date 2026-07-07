package lumien.randomthings.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import lumien.randomthings.client.renderer.DiviningRodRenderer;
import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spectre Illuminator: render blocks in illuminated chunks at full brightness.
 * Divining Rod: render ore-highlight overlays at the end of the level render pass.
 * Rain Shield: suppress rain rendering, rain particles and rain sounds in protected areas.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    private void randomthings$rainShieldHidesRain(LightTexture lightTexture, float partialTick,
                                                  double camX, double camY, double camZ, CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null && mc.player != null
            && !lumien.randomthings.blockentity.RainShieldBlockEntity.shouldRain(mc.level, mc.player.blockPosition())) {
            ci.cancel();
        }
    }

    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    private void randomthings$rainShieldMutesRain(Camera camera, CallbackInfo ci) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null
            && !lumien.randomthings.blockentity.RainShieldBlockEntity.shouldRain(mc.level, camera.getBlockPosition())) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void randomthings$renderOverlays(PoseStack poseStack, float partialTick, long finishNanoTime,
                                             boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                             LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        DiviningRodRenderer.renderOverlays(poseStack);
    }

    @Inject(
        method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
        at = @At("HEAD"),
        cancellable = true)
    private static void randomthings$illuminate(BlockAndTintGetter level, BlockState state, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (SpectreIlluminatorEntity.isChunkIlluminatedStatic(pos)) {
            cir.setReturnValue(LightTexture.pack(15, 15));
        }
    }
}
