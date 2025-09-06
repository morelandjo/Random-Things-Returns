package lumien.randomthings.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
// import lumien.randomthings.blockentity.BasicRedstoneInterfaceBlockEntity;
import lumien.randomthings.item.ModDataComponents;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class RedstoneInterfaceRenderer {

    public static void renderRedstoneInterfaceLines(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 cameraPos) {
        /*
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(ModItems.REDSTONE_TOOL.get())) return;
        
        Level level = player.level();
        List<BasicRedstoneInterfaceBlockEntity> interfaces = new ArrayList<>();
        
        // Collect all interfaces in the world
        for (BasicRedstoneInterfaceBlockEntity blockEntity : BasicRedstoneInterfaceBlockEntity.getAllInterfaces()) {
            if (!blockEntity.isRemoved() && blockEntity.getLevel() == level) {
                interfaces.add(blockEntity);
            }
        }
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        
        // Draw lines from each interface to its target
        for (BasicRedstoneInterfaceBlockEntity redstoneInterface : interfaces) {
            BlockPos target = redstoneInterface.getTarget();
            if (target != null) {
                BlockPos interfacePos = redstoneInterface.getBlockPos();
                
                // Calculate line start and end positions relative to camera
                double startX = interfacePos.getX() + 0.5 - cameraPos.x;
                double startY = interfacePos.getY() + 0.5 - cameraPos.y;
                double startZ = interfacePos.getZ() + 0.5 - cameraPos.z;
                
                double endX = target.getX() + 0.5 - cameraPos.x;
                double endY = target.getY() + 0.5 - cameraPos.y;
                double endZ = target.getZ() + 0.5 - cameraPos.z;
                
                // Draw red line
                vertexConsumer.addVertex(poseStack.last().pose(), (float) startX, (float) startY, (float) startZ)
                    .setColor(1.0f, 0.0f, 0.0f, 0.8f); // Red with transparency
                    
                vertexConsumer.addVertex(poseStack.last().pose(), (float) endX, (float) endY, (float) endZ)
                    .setColor(1.0f, 0.0f, 0.0f, 0.8f); // Red with transparency
            }
        }
        
        // Draw linking cube if in linking mode - temporarily disabled
        // boolean isLinking = heldItem.getOrDefault(ModDataComponents.REDSTONE_TOOL_LINKING.get(), false);
        // if (isLinking) {
        //     BlockPos linkingPos = heldItem.get(ModDataComponents.REDSTONE_TOOL_TARGET.get());
        //     if (linkingPos != null) {
        //         drawLinkingCube(poseStack, bufferSource, linkingPos, cameraPos);
        //     }
        // }
        */
    }
    
    private static void drawLinkingCube(PoseStack poseStack, MultiBufferSource bufferSource, BlockPos pos, Vec3 cameraPos) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.LINES);
        Matrix4f matrix = poseStack.last().pose();
        
        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;
        
        float expand = 0.02f; // Slightly larger than the block
        float minX = (float) x - expand;
        float minY = (float) y - expand;
        float minZ = (float) z - expand;
        float maxX = (float) x + 1.0f + expand;
        float maxY = (float) y + 1.0f + expand;
        float maxZ = (float) z + 1.0f + expand;
        
        // Draw cube outline in red
        float red = 0.8f;
        float green = 0.0f;
        float blue = 0.0f;
        float alpha = 0.6f;
        
        // Bottom face
        drawLine(vertexConsumer, matrix, poseStack, minX, minY, minZ, maxX, minY, minZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, minY, minZ, maxX, minY, maxZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, minY, maxZ, minX, minY, maxZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, minX, minY, maxZ, minX, minY, minZ, red, green, blue, alpha);
        
        // Top face
        drawLine(vertexConsumer, matrix, poseStack, minX, maxY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, maxY, minZ, maxX, maxY, maxZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, maxY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, minX, maxY, maxZ, minX, maxY, minZ, red, green, blue, alpha);
        
        // Vertical edges
        drawLine(vertexConsumer, matrix, poseStack, minX, minY, minZ, minX, maxY, minZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, minY, minZ, maxX, maxY, minZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, maxX, minY, maxZ, maxX, maxY, maxZ, red, green, blue, alpha);
        drawLine(vertexConsumer, matrix, poseStack, minX, minY, maxZ, minX, maxY, maxZ, red, green, blue, alpha);
    }
    
    private static void drawLine(VertexConsumer vertexConsumer, Matrix4f matrix, PoseStack poseStack, 
                                float x1, float y1, float z1, float x2, float y2, float z2, 
                                float red, float green, float blue, float alpha) {
        vertexConsumer.addVertex(matrix, x1, y1, z1)
            .setColor(red, green, blue, alpha);
            
        vertexConsumer.addVertex(matrix, x2, y2, z2)
            .setColor(red, green, blue, alpha);
    }
}