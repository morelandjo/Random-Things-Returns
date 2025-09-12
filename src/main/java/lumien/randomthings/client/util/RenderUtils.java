package lumien.randomthings.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class RenderUtils {
    
    public static void enableDefaultBlending() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    public static void drawCube(PoseStack poseStack, MultiBufferSource bufferSource, 
                               float x, float y, float z, float size, 
                               int red, int green, int blue, int alpha) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());
        Matrix4f matrix = poseStack.last().pose();
        
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        float a = alpha / 255.0f;
        
        // Draw all 6 faces of the cube
        // Front face
        buffer.addVertex(matrix, x, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z + size).setColor(r, g, b, a);
        
        // Back face
        buffer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a);
        
        // Left face
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        
        // Right face
        buffer.addVertex(matrix, x + size, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        
        // Top face
        buffer.addVertex(matrix, x, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        
        // Bottom face
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, z + size).setColor(r, g, b, a);
    }
    
    public static void drawWireframeCube(PoseStack poseStack, MultiBufferSource bufferSource,
                                        float x, float y, float z, float size,
                                        int red, int green, int blue, int alpha) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugLineStrip(1.0));
        Matrix4f matrix = poseStack.last().pose();
        
        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        float a = alpha / 255.0f;
        
        // Draw the 12 edges of the cube
        // Bottom face edges
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        
        // Top face edges
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        
        // Vertical edges
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, x + size, y, z).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, x + size, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        
        buffer.addVertex(matrix, x, y, z + size).setColor(r, g, b, a);
        buffer.addVertex(matrix, x, y + size, z + size).setColor(r, g, b, a);
    }
}
