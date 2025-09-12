package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SpiritRenderer extends MobRenderer<SpiritEntity, SlimeModel<SpiritEntity>> {
    private static final ResourceLocation SPIRIT_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/entity/spirit.png");

    public SpiritRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel<>(context.bakeLayer(ModelLayers.SLIME)), 0.25F);
    }

    @Override
    public void render(SpiritEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.shadowRadius = 0.25F * 0.5F;
        
        // Render the main entity with transparency
        poseStack.pushPose();
        
        // Scale down the entity
        float scale = 0.5F;
        poseStack.scale(scale, scale, scale);
        
        // Get translucent render type
        RenderType renderType = RenderType.entityTranslucent(SPIRIT_TEXTURE);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        // Set up the model
        this.model.prepareMobModel(entity, 0, 0, partialTicks);
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);
        
        // Render with transparency (70% alpha = 30% transparent)
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY); 
        
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(SpiritEntity entity) {
        return SPIRIT_TEXTURE;
    }
}