package lumien.randomthings.client.renderer;

import java.awt.Color;

import lumien.randomthings.client.render.magiccircles.ColorFunctions;
import lumien.randomthings.client.render.magiccircles.IColorFunction;
import lumien.randomthings.client.render.magiccircles.ITriangleFunction;
import lumien.randomthings.client.util.MKRRenderUtil;
import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import lumien.randomthings.event.RTEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TimeAcceleratorRenderer extends EntityRenderer<TimeAcceleratorEntity> {
    
    // Original 1.12.2 color functions - light gray/white based
    IColorFunction innerFunction = ColorFunctions.alternateN(new Color(100, 100, 100, 80), Color.LIGHT_GRAY, 6, 2);

    IColorFunction outerFunction1 = ColorFunctions.constant(Color.LIGHT_GRAY).next(ColorFunctions.flicker(50, 100)).next(ColorFunctions.limit(ColorFunctions.constant(new Color(0, 0, 0, 0)), (i) -> {
        return (i + 2) % 6 != 0;
    }));
    
    IColorFunction outerFunction2 = ColorFunctions.constant(Color.LIGHT_GRAY).next(ColorFunctions.limit(ColorFunctions.constant(new Color(0, 0, 0, 0)), (i) -> {
        return (i + 2) % 6 == 0 || (i+4) % 6 == 0 || (i+6) % 6 == 0;
    })).next(ColorFunctions.flicker(400, 100));

    IColorFunction outerFunction4 = ColorFunctions.alternateN(new Color(100, 100, 100, 80), Color.LIGHT_GRAY, 2, 5).next(ColorFunctions.flicker(800, 100)).next(ColorFunctions.limit(ColorFunctions.constant(new Color(100, 100, 100, 80)), (i) -> {
        return (i) % 2 == 0 || ((i) / 5) % 2 == 1;
    }));
    
    public TimeAcceleratorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    
    @Override
    public void render(TimeAcceleratorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        
        // Set up OpenGL state like the original 1.12.2 implementation
        RenderUtils.enableDefaultBlending();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableCull();
        
        // Disable lightmap like original
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        
        poseStack.pushPose();
        
        int timeRate = entity.getTimeRate();
        float progress = (2) * (RTEventHandler.clientAnimationCounter + partialTicks);
        
        
        // Original 1.12.2: Render magic circles on ALL 6 FACES of the block
        for (Direction facing : Direction.values()) {
            poseStack.translate(facing.getStepX() / 1.9D, facing.getStepY() / 1.9D, facing.getStepZ() / 1.9D);

            float rotX = facing.getStepX();
            float rotY = facing.getStepY(); 
            float rotZ = facing.getStepZ();

            poseStack.mulPose(com.mojang.math.Axis.of(new Vector3f(rotZ, rotY, rotX)).rotationDegrees(90));
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(progress));
            
            // Setup render context for this face
            MKRRenderUtil.setRenderContext(poseStack, bufferSource);

            // Always render inner circle - original 1.12.2 sizes
            MKRRenderUtil.renderCircleDecTriInner(0.05, innerFunction.tt(progress), 33, (i) -> {
                return 3;
            });

            if (timeRate >= 2) {
                MKRRenderUtil.renderCircleDecTriPartCross(0.07, 0.05, ColorFunctions.constant(new Color(100, 100, 100, 80)).next(ColorFunctions.limit(ColorFunctions.constant(Color.LIGHT_GRAY), (i) -> {
                    i += 6;
                    return i % 6 < 4 && i / 6 % 2 != 0;
                })).tt(progress), 60);
            }

            if (timeRate >= 4) {
                // Original: 0.1, 0.07
                MKRRenderUtil.renderCircleDecTriPart3Tri(0.1, 0.07, outerFunction1.tt(progress), 30);
            }

            if (timeRate >= 8) {
                // Original: 0.12, 0.09
                MKRRenderUtil.renderCircleDecTriPart3Tri(0.12, 0.09, outerFunction2.tt(progress), 30);
            }

            if (timeRate >= 16) {
                // Original: 0.15, 0.12
                MKRRenderUtil.renderCircleDecTriPart5Tri(0.15, 0.12, outerFunction4.tt(progress), 50);
            }

            if (timeRate >= 32) {
                // Original: 0.16, 0.15
                MKRRenderUtil.renderCircleDecTriPart5Tri(0.16, 0.15, (i) -> {
                    Color c = Color.getHSBColor(0, 0, (float) Math.sin(((Math.PI * 4) / 50F) * i + progress / 10) / 2.5F + 0.6F);
                    return new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
                }, 50);
            }

            // Reverse transformations for this face
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-progress));
            poseStack.mulPose(com.mojang.math.Axis.of(new Vector3f(rotZ, rotY, rotX)).rotationDegrees(-90));
            poseStack.translate(-facing.getStepX() / 1.9D, -facing.getStepY() / 1.9D, -facing.getStepZ() / 1.9D);
        }

        poseStack.popPose();
        
        // Restore OpenGL state
        RenderSystem.enableCull();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
    
    @Override
    public ResourceLocation getTextureLocation(TimeAcceleratorEntity entity) {
        return null; // No texture needed for this renderer
    }
}