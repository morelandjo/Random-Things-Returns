package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.Tesselator;
import lumien.randomthings.blockentity.DiaphanousBlockEntity;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class DiaphanousBlockRenderer implements BlockEntityRenderer<DiaphanousBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;
    private static long clientTicks = 0;

    public DiaphanousBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(DiaphanousBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        LocalPlayer player = Minecraft.getInstance().player;
        
        if (level == null || player == null) {
            return;
        }
        
        BlockState displayState = blockEntity.getDisplayState();
        
        if (displayState == null || displayState.getBlock() == Blocks.AIR) {
            return;
        }
        
        // Calculate distance from player
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        
        double dX = pos.getX() + 0.5 - playerX;
        double dY = pos.getY() + 0.5 - playerY;
        double dZ = pos.getZ() + 0.5 - playerZ;
        
        double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        float alpha = calculateAlpha(blockEntity, distance);
        
        // Don't render if completely transparent
        if (alpha <= 0.05f) {
            return;
        }
        
        poseStack.pushPose();
        
        // Manual vertex rendering with alpha - this bypasses Minecraft's batching system
        poseStack.pushPose();
        
        // Render each face of the cube manually with the calculated alpha
        renderCubeWithAlpha(poseStack, bufferSource.getBuffer(RenderType.translucent()), 
                           displayState, alpha, packedLight, packedOverlay);
        
        poseStack.popPose();
        
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(DiaphanousBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
    
    // Static method to increment client ticks for animation
    public static void tick() {
        clientTicks++;
    }
    
    private float calculateAlpha(DiaphanousBlockEntity blockEntity, double distance) {
        float alpha;
        
        if (blockEntity.isItem()) {
            // Animated alpha for item form
            alpha = (float) (Math.sin(clientTicks / 20.0) * 0.3 + 0.3 + 0.4);
        } else {
            // Original distance-based alpha calculation from 1.12.2
            double distanceForCalc = Math.max(0, distance - 3); // Start fade at 3 blocks
            float cosValue = (float) Math.cos(Math.PI * Math.min(distanceForCalc, 8) / 8);
            alpha = -0.5f * (cosValue - 1);
            
            if (blockEntity.isInverted()) {
                alpha = 1.0f - alpha;
            }
            
        }
        
        // Check if player is holding diaphanous block - make it always visible
        LocalPlayer player = Minecraft.getInstance().player;
        boolean holdingDiaphanousBlock = false;
        if (player != null) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = player.getItemInHand(hand);
                if (!held.isEmpty() && held.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    holdingDiaphanousBlock = true;
                    break;
                }
            }
        }
        
        if (holdingDiaphanousBlock) {
            alpha = 0.8f; // Still translucent but visible when holding
        }
        
        // Clamp alpha
        return Math.max(0.05f, Math.min(1.0f, alpha));
    }
    
    private void renderCubeWithAlpha(PoseStack poseStack, VertexConsumer consumer, 
                                   BlockState displayState, float alpha, int packedLight, int packedOverlay) {
        // Get the stone texture sprite
        TextureAtlasSprite stoneSprite = Minecraft.getInstance()
            .getBlockRenderer()
            .getBlockModel(displayState)
            .getParticleIcon();
            
        if (stoneSprite == null) {
            // Fallback - get stone texture directly
            stoneSprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(ResourceLocation.fromNamespaceAndPath("minecraft", "block/stone"));
        }
        
        // Get the transformation matrix
        var matrix = poseStack.last().pose();
        var normal = poseStack.last().normal();
        
        // Convert alpha to int (0-255)
        int alphaInt = (int)(alpha * 255);
        
        // Get texture coordinates
        float u0 = stoneSprite.getU0();
        float u1 = stoneSprite.getU1();
        float v0 = stoneSprite.getV0();
        float v1 = stoneSprite.getV1();
        
        // Render 6 faces of the cube with proper texture coordinates
        // Front face (Z=1)
        addQuad(consumer, matrix, normal, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
        
        // Back face (Z=0) 
        addQuad(consumer, matrix, normal, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0,
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
               
        // Top face (Y=1)
        addQuad(consumer, matrix, normal, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0,
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
               
        // Bottom face (Y=0)
        addQuad(consumer, matrix, normal, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1,
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
               
        // Right face (X=1)
        addQuad(consumer, matrix, normal, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1,
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
               
        // Left face (X=0)
        addQuad(consumer, matrix, normal, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0,
               u0, v0, u1, v0, u1, v1, u0, v1, alphaInt, packedLight, packedOverlay);
    }
    
    private void addQuad(VertexConsumer consumer, org.joml.Matrix4f matrix, org.joml.Matrix3f normal,
                        float x1, float y1, float z1, float x2, float y2, float z2,
                        float x3, float y3, float z3, float x4, float y4, float z4,
                        float u1, float v1, float u2, float v2, float u3, float v3, float u4, float v4,
                        int alpha, int packedLight, int packedOverlay) {
        // Add 4 vertices for a quad (2 triangles)
        // Triangle 1: v1, v2, v3
        // Triangle 2: v1, v3, v4
        
        // Vertex 1
        consumer.addVertex(matrix, x1, y1, z1)
               .setColor(255, 255, 255, alpha)
               .setUv(u1, v1)
               .setOverlay(packedOverlay)
               .setLight(packedLight)
               .setNormal(0.0f, 0.0f, 1.0f);
               
        // Vertex 2  
        consumer.addVertex(matrix, x2, y2, z2)
               .setColor(255, 255, 255, alpha)
               .setUv(u2, v2)
               .setOverlay(packedOverlay)
               .setLight(packedLight)
               .setNormal(0.0f, 0.0f, 1.0f);
               
        // Vertex 3
        consumer.addVertex(matrix, x3, y3, z3)
               .setColor(255, 255, 255, alpha)
               .setUv(u3, v3)
               .setOverlay(packedOverlay)
               .setLight(packedLight)
               .setNormal(0.0f, 0.0f, 1.0f);
               
        // Vertex 4 (complete the quad)
        consumer.addVertex(matrix, x4, y4, z4)
               .setColor(255, 255, 255, alpha)
               .setUv(u4, v4)
               .setOverlay(packedOverlay)
               .setLight(packedLight)
               .setNormal(0.0f, 0.0f, 1.0f);
    }
}