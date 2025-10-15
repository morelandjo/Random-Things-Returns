package lumien.randomthings.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ArtificialEndPortalRenderer extends EntityRenderer<ArtificialEndPortalEntity> {
    private static final ResourceLocation END_SKY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/end_portal.png");

    public ArtificialEndPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ArtificialEndPortalEntity entity, float entityYaw, float partialTicks,
                      PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Only render after tick 85 when the portal starts opening
        if (entity.actionTimer <= 85) {
            return;
        }

        // Calculate portal size (grows from 0 to 3 blocks)
        float progress = Math.min(1.0f, (entity.actionTimer + partialTicks - 85) / 115.0f);
        float size = 3.0f * progress;
        float halfSize = size / 2.0f;

        poseStack.pushPose();

        // The portal should be rendered at the entity's position
        // No translation needed as we're already at the entity position

        // Render the portal effect using End Portal render type
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();

        // Render a horizontal quad for the portal surface
        float y = 0.01f; // Slightly above the ground to avoid z-fighting

        // Draw the portal quad (horizontal surface)
        vertexConsumer.addVertex(matrix, -halfSize, y, -halfSize).setColor(255, 255, 255, 255).setUv(0, 0);
        vertexConsumer.addVertex(matrix, -halfSize, y, halfSize).setColor(255, 255, 255, 255).setUv(0, 1);
        vertexConsumer.addVertex(matrix, halfSize, y, halfSize).setColor(255, 255, 255, 255).setUv(1, 1);
        vertexConsumer.addVertex(matrix, halfSize, y, -halfSize).setColor(255, 255, 255, 255).setUv(1, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ArtificialEndPortalEntity entity) {
        return END_PORTAL_TEXTURE;
    }
}
