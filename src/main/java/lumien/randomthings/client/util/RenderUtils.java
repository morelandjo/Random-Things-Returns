package lumien.randomthings.client.util;

import java.util.function.Function;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class RenderUtils {
    
    // Create a custom render type that's guaranteed to be visible
    private static final RenderType DIVINING_OVERLAY = RenderType.create(
        "divining_rod_overlay",
        DefaultVertexFormat.POSITION_COLOR,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RenderType.POSITION_COLOR_SHADER)
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderType.NO_CULL)
            .setLightmapState(RenderType.NO_LIGHTMAP)
            .setOverlayState(RenderType.NO_OVERLAY)
            .setDepthTestState(RenderType.NO_DEPTH_TEST) // Render through blocks
            .setWriteMaskState(RenderType.COLOR_WRITE)
            .createCompositeState(false)
    );
    
    public static void drawFunctionLinePart(Function<Float, Vector3f> function, float lineLength, float progress, 
                                          PoseStack poseStack, MultiBufferSource bufferSource) {
        
        // Setup numbers
        float neededProgress = progress + lineLength;
        float mappedProgress = progress * neededProgress;
        
        float progressStart = Math.max(0, mappedProgress - lineLength);
        float progressEnd = Math.min(mappedProgress, 1);
        
        float range = progressEnd - progressStart;
        
        int lineSegments = 100;
        float step = range / lineSegments;
        
        if (range == 0) return;
        
        // Get line render type
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        
        // Render line segments
        for (int seg = 0; seg < lineSegments - 1; seg++) {
            float p1 = progressStart + seg * step;
            float p2 = progressStart + (seg + 1) * step;
            
            Vector3f vec1 = function.apply(p1);
            Vector3f vec2 = function.apply(p2);
            
            float alpha = seg > lineSegments - 10 ? (10 - (seg - (lineSegments - 10))) / 10f : 1f;
            alpha = 0.5F;
            
            // Draw line segment
            vertexConsumer.addVertex(poseStack.last().pose(), vec1.x, vec1.y, vec1.z)
                         .setColor(1.0f, 0.0f, 0.0f, alpha);
            vertexConsumer.addVertex(poseStack.last().pose(), vec2.x, vec2.y, vec2.z)
                         .setColor(1.0f, 0.0f, 0.0f, alpha);
        }
    }
    
    public static Function<Float, Vector3f> getLineFunction(Vector3f from, Vector3f to) {
        return (progress) -> {
            return new Vector3f(from).lerp(to, progress);
        };
    }
    
    public static void drawCube(PoseStack poseStack, MultiBufferSource bufferSource, 
                               float x, float y, float z, float size, 
                               int red, int green, int blue, int alpha) {
        
        VertexConsumer consumer = bufferSource.getBuffer(DIVINING_OVERLAY);
        
        float minX = x;
        float minY = y;
        float minZ = z;
        float maxX = x + size;
        float maxY = y + size;
        float maxZ = z + size;
        
        float r = red / 255.0F;
        float g = green / 255.0F;
        float b = blue / 255.0F;
        float a = alpha / 255.0F;
        
        // Draw cube faces with POSITION_COLOR format for debug renderer
        // Bottom face (Y-)
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
        
        // Top face (Y+)
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
        
        // North face (Z-)
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
        
        // South face (Z+)
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
        
        // West face (X-)
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a);
        
        // East face (X+)
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a);
    }
    
    public static void drawWireframeCube(PoseStack poseStack, MultiBufferSource bufferSource, 
                                        float x, float y, float z, float size, 
                                        int red, int green, int blue, int alpha) {
        
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        
        float minX = x;
        float minY = y;
        float minZ = z;
        float maxX = x + size;
        float maxY = y + size;
        float maxZ = z + size;
        
        float r = red / 255.0F;
        float g = green / 255.0F;
        float b = blue / 255.0F;
        float a = alpha / 255.0F;
        
        // Draw wireframe cube with lines - lines render type needs normals
        // Bottom edges
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        // Top edges
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        // Vertical edges
        consumer.addVertex(poseStack.last().pose(), minX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, minY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, minZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), maxX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        
        consumer.addVertex(poseStack.last().pose(), minX, minY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
        consumer.addVertex(poseStack.last().pose(), minX, maxY, maxZ).setColor(r, g, b, a).setNormal(0, 1, 0);
    }
}