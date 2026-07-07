package lumien.randomthings.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class RenderUtils {

    public static void drawCube(PoseStack poseStack, MultiBufferSource bufferSource,
                               float x, float y, float z, float size,
                               int red, int green, int blue, int alpha) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.debugQuads());
        Matrix4f matrix = poseStack.last().pose();

        float r = red / 255.0f;
        float g = green / 255.0f;
        float b = blue / 255.0f;
        float a = alpha / 255.0f;

        // Front face
        buffer.vertex(matrix, x, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z + size).color(r, g, b, a).endVertex();

        // Back face
        buffer.vertex(matrix, x + size, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z).color(r, g, b, a).endVertex();

        // Left face
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();

        // Right face
        buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z + size).color(r, g, b, a).endVertex();

        // Top face
        buffer.vertex(matrix, x, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();

        // Bottom face
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + size).color(r, g, b, a).endVertex();
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

        // Bottom face edges
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();

        // Top face edges
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();

        // Vertical edges (line strip restarts via duplicate vertices)
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x + size, y, z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x + size, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x + size, y + size, z + size).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, x, y, z + size).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, x, y + size, z + size).color(r, g, b, a).endVertex();
    }
}
