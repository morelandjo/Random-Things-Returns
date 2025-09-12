package lumien.randomthings.client.util;

import java.awt.Color;
import java.util.function.Function;

import lumien.randomthings.client.render.magiccircles.ITriangleFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;

public class MKRRenderUtil {

    private static VertexConsumer currentBuffer;
    private static Matrix4f currentMatrix;

    public static void setRenderContext(PoseStack poseStack, MultiBufferSource bufferSource) {
        currentMatrix = poseStack.last().pose();
        
        // Set up OpenGL state for line rendering
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
    }

    public static void renderCircleDecTriInner(double r, ITriangleFunction triangleFunction, int triCount) {
        renderCircleDecTriInner(r, triangleFunction, triCount, (i) -> 1);
    }

    // triCount <= 11
    public static void renderCircleDecTriInner(double r, ITriangleFunction triangleFunction, int triCount, Function<Integer, Integer> countFunction) {
        if (currentMatrix == null) {
            return;
        }
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        
        // Convert original GL_TRIANGLE_FAN approach to individual triangles
        int currentTriCount = 0;
        for (int c = 0; c < 10; c++) {
            int countForC = countFunction.apply(c);

            double winkelPart = 36 / countForC;
            double winkel = Math.PI * 2 / 360 * c * 36;
            double winkel2 = Math.PI * 2 / 360 * (c + 1) * 36;

            double pX = Math.sin(winkel) * (r);
            double pZ = Math.cos(winkel) * (r);
            double pX2 = Math.sin(winkel2) * (r);
            double pZ2 = Math.cos(winkel2) * (r);

            double dX = (pX2 - pX);
            double dZ = (pZ2 - pZ);
            double length = Math.sqrt(dX * dX + dZ * dZ);
            double partLength = length / countForC;
            double nX = dX / length;
            double nZ = dZ / length;

            // First vertex at edge
            double currentX = pX;
            double currentZ = pZ;

            for (int p = 1; p <= countForC; p++) {
                if (currentTriCount < triCount) {
                    Color color = triangleFunction.apply(currentTriCount);
                    double nextX = pX + nX * partLength * p;
                    double nextZ = pZ + nZ * partLength * p;

                    // Create triangle: center -> current -> next
                    addVertexImmediate(buffer, 0, 0, 0, color);
                    addVertexImmediate(buffer, currentX, 0, currentZ, color);
                    addVertexImmediate(buffer, nextX, 0, nextZ, color);

                    currentX = nextX;
                    currentZ = nextZ;
                    currentTriCount++;
                }
            }
        }
        
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void renderCircleDecTriPart5Tri(double r1, double r2, ITriangleFunction triangleFunction, int triCount) {
        renderCircleDecTriPart5Tri(r1, r2, triangleFunction, triCount, 0, 10);
    }

    // Tricount <= 50
    public static void renderCircleDecTriPart5Tri(double r1, double r2, ITriangleFunction triangleFunction, int triCount, int cStart, int cEnd) {
        if (currentMatrix == null) {
            return;
        }
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int c = cStart; c < cEnd; c++) {
            int winkelDeg1 = 36 * c;
            int winkelDeg2 = 36 * (c + 1);

            int triIndex = (c - cStart) * 5;

            double winkelRad1 = (Math.PI * 2) / 360D * winkelDeg1;
            double winkelRad2 = (Math.PI * 2) / 360D * winkelDeg2;

            double pX1 = Math.sin(winkelRad1) * (r1);
            double pZ1 = Math.cos(winkelRad1) * (r1);

            double pX2 = Math.sin(winkelRad2) * (r1);
            double pZ2 = Math.cos(winkelRad2) * (r1);

            double pX1H = Math.sin(winkelRad1) * (r2);
            double pZ1H = Math.cos(winkelRad1) * (r2);

            double pX2H = Math.sin(winkelRad2) * (r2);
            double pZ2H = Math.cos(winkelRad2) * (r2);

            // Create 5 triangles between the two radii in a quad strip pattern
            Color color1 = triangleFunction.apply(Math.min(triIndex, triCount - 1));
            Color color2 = triangleFunction.apply(Math.min(triIndex + 1, triCount - 1));
            Color color3 = triangleFunction.apply(Math.min(triIndex + 2, triCount - 1));
            Color color4 = triangleFunction.apply(Math.min(triIndex + 3, triCount - 1));
            Color color5 = triangleFunction.apply(Math.min(triIndex + 4, triCount - 1));

            // Triangle 1: Inner arc
            addVertexImmediate(buffer, pX1, 0, pZ1, color1);
            addVertexImmediate(buffer, pX2, 0, pZ2, color1);
            addVertexImmediate(buffer, (pX1 + pX2) / 2, 0, (pZ1 + pZ2) / 2, color1);

            // Triangle 2: Outer arc
            addVertexImmediate(buffer, pX1H, 0, pZ1H, color2);
            addVertexImmediate(buffer, pX2H, 0, pZ2H, color2);
            addVertexImmediate(buffer, (pX1H + pX2H) / 2, 0, (pZ1H + pZ2H) / 2, color2);

            // Triangle 3: Left side connection
            addVertexImmediate(buffer, pX1, 0, pZ1, color3);
            addVertexImmediate(buffer, pX1H, 0, pZ1H, color3);
            addVertexImmediate(buffer, (pX1 + pX1H) / 2, 0, (pZ1 + pZ1H) / 2, color3);

            // Triangle 4: Right side connection
            addVertexImmediate(buffer, pX2, 0, pZ2, color4);
            addVertexImmediate(buffer, pX2H, 0, pZ2H, color4);
            addVertexImmediate(buffer, (pX2 + pX2H) / 2, 0, (pZ2 + pZ2H) / 2, color4);

            // Triangle 5: Center fill if we haven't exceeded triangle count
            if (triIndex + 4 < triCount) {
                double centerX = (pX1 + pX2 + pX1H + pX2H) / 4;
                double centerZ = (pZ1 + pZ2 + pZ1H + pZ2H) / 4;
                addVertexImmediate(buffer, pX1, 0, pZ1, color5);
                addVertexImmediate(buffer, pX2H, 0, pZ2H, color5);
                addVertexImmediate(buffer, centerX, 0, centerZ, color5);
            }
        }
        
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void renderCircleDecTriPart3Tri(double r1, double r2, ITriangleFunction triangleFunction, int triCount) {
        renderCircleDecTriPart3Tri(r1, r2, triangleFunction, triCount, 0, 10);
    }

    // triCount <= 30
    public static void renderCircleDecTriPart3Tri(double r1, double r2, ITriangleFunction triangleFunction, int triCount, int cStart, int cEnd) {
        if (currentMatrix == null) {
            return;
        }
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int c = cStart; c < cEnd; c++) {
            int winkelDeg1 = 36 * c;
            int winkelDeg2 = 36 * (c + 1);

            int triIndex = (c - cStart) * 3;

            double winkelRad1 = (Math.PI * 2) / 360D * winkelDeg1;
            double winkelRad2 = (Math.PI * 2) / 360D * winkelDeg2;

            double pX1 = Math.sin(winkelRad1) * (r1);
            double pZ1 = Math.cos(winkelRad1) * (r1);

            double pX2 = Math.sin(winkelRad2) * (r1);
            double pZ2 = Math.cos(winkelRad2) * (r1);

            double pX1H = Math.sin(winkelRad1) * (r2);
            double pZ1H = Math.cos(winkelRad1) * (r2);

            double pX2H = Math.sin(winkelRad2) * (r2);
            double pZ2H = Math.cos(winkelRad2) * (r2);

            Color color1 = triangleFunction.apply(Math.min(triIndex, triCount - 1));
            Color color2 = triangleFunction.apply(Math.min(triIndex + 1, triCount - 1));
            Color color3 = triangleFunction.apply(Math.min(triIndex + 2, triCount - 1));

            // Triangle 1: Inner-outer connection
            addVertexImmediate(buffer, pX1, 0, pZ1, color1);
            addVertexImmediate(buffer, pX1H, 0, pZ1H, color1);
            addVertexImmediate(buffer, pX2, 0, pZ2, color1);

            // Triangle 2: Outer-inner connection
            addVertexImmediate(buffer, pX1H, 0, pZ1H, color2);
            addVertexImmediate(buffer, pX2H, 0, pZ2H, color2);
            addVertexImmediate(buffer, pX2, 0, pZ2, color2);

            // Triangle 3: Center connection
            addVertexImmediate(buffer, pX1H, 0, pZ1H, color3);
            addVertexImmediate(buffer, pX2H, 0, pZ2H, color3);
            addVertexImmediate(buffer, (pX1 + pX2H) / 2, 0, (pZ1 + pZ2H) / 2, color3);
        }
        
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    public static void renderCircleDecTriPartCross(double r1, double r2, ITriangleFunction triangleFunction, int triCount) {
        if (currentMatrix == null) {
            return;
        }
        
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        for (int c = 0; c < 10; c++) {
            int winkelDeg1 = 36 * c;
            int winkelDeg2 = 36 * (c + 1);

            int triIndex = c * 6;

            double winkelRad1 = (Math.PI * 2) / 360D * winkelDeg1;
            double winkelRad2 = (Math.PI * 2) / 360D * winkelDeg2;

            double pX1 = Math.sin(winkelRad1) * (r1);
            double pZ1 = Math.cos(winkelRad1) * (r1);

            double pX2 = Math.sin(winkelRad2) * (r1);
            double pZ2 = Math.cos(winkelRad2) * (r1);

            double pX1N = Math.sin(winkelRad1) * (r2);
            double pZ1N = Math.cos(winkelRad1) * (r2);

            double pX2N = Math.sin(winkelRad2) * (r2);
            double pZ2N = Math.cos(winkelRad2) * (r2);

            // Cross pattern - 6 triangles per segment
            for (int t = 0; t < 6 && triIndex + t < triCount; t++) {
                Color color = triangleFunction.apply(triIndex + t);

                // Create cross pattern triangles
                switch (t) {
                    case 0:
                        // Inner to outer radial line 1
                        addVertexImmediate(buffer, pX1, 0, pZ1, color);
                        addVertexImmediate(buffer, pX1N, 0, pZ1N, color);
                        addVertexImmediate(buffer, (pX1 + pX2) / 2, 0, (pZ1 + pZ2) / 2, color);
                        break;
                    case 1:
                        // Inner to outer radial line 2
                        addVertexImmediate(buffer, pX2, 0, pZ2, color);
                        addVertexImmediate(buffer, pX2N, 0, pZ2N, color);
                        addVertexImmediate(buffer, (pX1 + pX2) / 2, 0, (pZ1 + pZ2) / 2, color);
                        break;
                    case 2:
                        // Cross connection 1
                        addVertexImmediate(buffer, pX1, 0, pZ1, color);
                        addVertexImmediate(buffer, pX2N, 0, pZ2N, color);
                        addVertexImmediate(buffer, (pX1N + pX2) / 2, 0, (pZ1N + pZ2) / 2, color);
                        break;
                    case 3:
                        // Cross connection 2
                        addVertexImmediate(buffer, pX2, 0, pZ2, color);
                        addVertexImmediate(buffer, pX1N, 0, pZ1N, color);
                        addVertexImmediate(buffer, (pX1 + pX2N) / 2, 0, (pZ1 + pZ2N) / 2, color);
                        break;
                    case 4:
                        // Outer arc segment 1
                        addVertexImmediate(buffer, pX1N, 0, pZ1N, color);
                        addVertexImmediate(buffer, pX2N, 0, pZ2N, color);
                        addVertexImmediate(buffer, (pX1N + pX2N) / 2, 0, (pZ1N + pZ2N) / 2, color);
                        break;
                    case 5:
                        // Inner arc segment
                        addVertexImmediate(buffer, pX1, 0, pZ1, color);
                        addVertexImmediate(buffer, pX2, 0, pZ2, color);
                        addVertexImmediate(buffer, (pX1 + pX2) / 2, 0, (pZ1 + pZ2) / 2, color);
                        break;
                }
            }
        }
        
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void addVertexImmediate(BufferBuilder buffer, double x, double y, double z, Color color) {
        if (currentMatrix != null) {
            float r = color.getRed() / 255.0f;
            float g = color.getGreen() / 255.0f;
            float b = color.getBlue() / 255.0f;
            float a = color.getAlpha() / 255.0f;
            
            // Add vertex with matrix transformation - correct API for 1.21.1
            buffer.addVertex(currentMatrix, (float) x, (float) y, (float) z)
                    .setColor(r, g, b, a);
        }
    }

    // Legacy method for other render methods - simplified for now
    private static void addVertex(double x, double y, double z, Color color) {
        // This will be called by other methods that haven't been converted yet
        // For now, just do nothing to avoid crashes
    }
}