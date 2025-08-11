package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.blockentity.FluidDisplayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidDisplayBlockEntityRenderer implements BlockEntityRenderer<FluidDisplayBlockEntity> {
    
    public FluidDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(FluidDisplayBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        if (blockEntity == null) {
            return;
        }
        
        FluidStack fluid = blockEntity.getFluidStack();
        TextureAtlasSprite sprite;
        float red = 1.0f, green = 1.0f, blue = 1.0f, alpha = 1.0f;
        
        if (fluid == null || fluid.isEmpty()) {
            // Show default texture when no fluid is set
            sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(ResourceLocation.fromNamespaceAndPath("randomthings", "block/fluid_display"));
        } else {
            // Show fluid texture
            IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            ResourceLocation textureLocation;
            
            if (blockEntity.isFlowing()) {
                textureLocation = fluidExtensions.getFlowingTexture();
            } else {
                textureLocation = fluidExtensions.getStillTexture();
            }
            
            if (textureLocation == null) {
                return;
            }
            
            sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(textureLocation);
            
            // Get fluid color
            int color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
            red = ((color >> 16) & 0xFF) / 255.0f;
            green = ((color >> 8) & 0xFF) / 255.0f;
            blue = (color & 0xFF) / 255.0f;
            alpha = ((color >> 24) & 0xFF) / 255.0f;
        }
        
        VertexConsumer builder = bufferSource.getBuffer(RenderType.solid());
        
        poseStack.pushPose();
        
        // Apply rotation based on blockEntity.getRotation()
        switch (blockEntity.getRotation()) {
            case CLOCKWISE_90:
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.5, -0.5, -0.5);
                break;
            case CLOCKWISE_180:
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
                poseStack.translate(-0.5, -0.5, -0.5);
                break;
            case COUNTERCLOCKWISE_90:
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
                poseStack.translate(-0.5, -0.5, -0.5);
                break;
            case NONE:
            default:
                // No rotation
                break;
        }
        
        // Render all 6 faces of the cube with the texture
        renderCubeFace(builder, poseStack, Direction.NORTH, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        renderCubeFace(builder, poseStack, Direction.SOUTH, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        renderCubeFace(builder, poseStack, Direction.WEST, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        renderCubeFace(builder, poseStack, Direction.EAST, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        renderCubeFace(builder, poseStack, Direction.UP, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        renderCubeFace(builder, poseStack, Direction.DOWN, sprite, red, green, blue, alpha, packedLight, packedOverlay);
        
        poseStack.popPose();
    }
    
    private void renderCubeFace(VertexConsumer builder, PoseStack poseStack, Direction face, 
                               TextureAtlasSprite sprite, float red, float green, float blue, float alpha,
                               int packedLight, int packedOverlay) {
        
        var matrix = poseStack.last().pose();
        
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();
        
        switch (face) {
            case NORTH: // Counter-clockwise winding when viewed from outside
                builder.addVertex(matrix, 0, 1, 0).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 1, 1, 0).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 1, 0, 0).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 0, 0, 0).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, -1);
                break;
            case SOUTH: // Counter-clockwise winding when viewed from outside
                builder.addVertex(matrix, 1, 1, 1).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 0, 1, 1).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 0, 0, 1).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 1, 0, 1).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 0, 1);
                break;
            case WEST: // Counter-clockwise winding when viewed from outside
                builder.addVertex(matrix, 0, 1, 1).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, 0, 1, 0).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, 0, 0, 0).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, 0, 0, 1).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(-1, 0, 0);
                break;
            case EAST: // Counter-clockwise winding when viewed from outside
                builder.addVertex(matrix, 1, 1, 0).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1, 1, 1).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1, 0, 1).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1, 0, 0).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(1, 0, 0);
                break;
            case UP: // Counter-clockwise winding when viewed from above
                builder.addVertex(matrix, 0, 1, 1).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
                builder.addVertex(matrix, 1, 1, 1).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
                builder.addVertex(matrix, 1, 1, 0).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
                builder.addVertex(matrix, 0, 1, 0).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, 1, 0);
                break;
            case DOWN: // Counter-clockwise winding when viewed from below
                builder.addVertex(matrix, 0, 0, 0).setColor(red, green, blue, alpha).setUv(minU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
                builder.addVertex(matrix, 1, 0, 0).setColor(red, green, blue, alpha).setUv(maxU, minV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
                builder.addVertex(matrix, 1, 0, 1).setColor(red, green, blue, alpha).setUv(maxU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
                builder.addVertex(matrix, 0, 0, 1).setColor(red, green, blue, alpha).setUv(minU, maxV).setOverlay(packedOverlay).setLight(packedLight).setNormal(0, -1, 0);
                break;
        }
    }
}