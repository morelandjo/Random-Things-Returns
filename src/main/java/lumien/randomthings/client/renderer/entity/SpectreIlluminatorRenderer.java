package lumien.randomthings.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SpectreIlluminatorRenderer extends EntityRenderer<SpectreIlluminatorEntity> {
    public SpectreIlluminatorRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SpectreIlluminatorEntity entity) {
        return null; // No texture needed for procedural rendering
    }

    @Override
    public void render(SpectreIlluminatorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.pushPose();

        // Get the render type for lines (bright, unlit)
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());

        // Calculate animation progress
        float time = (entity.tickCount + partialTicks) * 0.05f;

        // Render multiple rotating rings
        for (int ring = 0; ring < 5; ring++) {
            poseStack.pushPose();

            // Random-ish rotation for each ring based on ring number
            float rotX = (ring * 73f + time * (1f + ring * 0.3f)) % 360f;
            float rotY = (ring * 41f + time * (0.8f + ring * 0.2f)) % 360f;
            float rotZ = (ring * 59f + time * (1.2f + ring * 0.25f)) % 360f;

            poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));

            // Varying radius for each ring
            float radius = 0.1f + ring * 0.08f;

            // Draw circle as line segments
            int segments = 24;
            for (int i = 0; i < segments; i++) {
                float angle1 = (float) (2 * Math.PI * i / segments);
                float angle2 = (float) (2 * Math.PI * (i + 1) / segments);

                float x1 = (float) (Math.cos(angle1) * radius);
                float y1 = (float) (Math.sin(angle1) * radius);
                float x2 = (float) (Math.cos(angle2) * radius);
                float y2 = (float) (Math.sin(angle2) * radius);

                // Color varies based on ring and time - cyan/blue spectrum
                float hue = (float) ((ring * 0.1f + time * 0.1f) % 1.0f);
                float brightness = 0.7f + (float) Math.sin(time + ring) * 0.3f;

                // Convert HSV to RGB (simplified for blue/cyan range)
                float r = 0.3f * brightness;
                float g = 0.7f * brightness;
                float b = 1.0f * brightness;
                float a = 0.9f;

                Matrix4f matrix = poseStack.last().pose();

                // Draw line segment
                vertexConsumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a).setNormal(0, 1, 0);
                vertexConsumer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a).setNormal(0, 1, 0);
            }

            poseStack.popPose();
        }

        // Render a central glowing core
        poseStack.pushPose();
        float coreRadius = 0.04f + (float) Math.sin(time * 2) * 0.01f;
        int coreSegments = 12;

        for (int i = 0; i < coreSegments; i++) {
            float angle1 = (float) (2 * Math.PI * i / coreSegments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / coreSegments);

            float x1 = (float) (Math.cos(angle1) * coreRadius);
            float y1 = (float) (Math.sin(angle1) * coreRadius);
            float x2 = (float) (Math.cos(angle2) * coreRadius);
            float y2 = (float) (Math.sin(angle2) * coreRadius);

            Matrix4f matrix = poseStack.last().pose();

            // Bright white/cyan core
            vertexConsumer.addVertex(matrix, x1, y1, 0).setColor(0.8f, 1.0f, 1.0f, 1.0f).setNormal(0, 1, 0);
            vertexConsumer.addVertex(matrix, x2, y2, 0).setColor(0.8f, 1.0f, 1.0f, 1.0f).setNormal(0, 1, 0);
        }

        poseStack.popPose();
        poseStack.popPose();
    }
}
