package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.blockentity.LightRedirectorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

import java.util.*;

public class LightRedirectorRenderer implements BlockEntityRenderer<LightRedirectorBlockEntity> {
    
    private final BlockRenderDispatcher blockRenderer;
    private static final Set<BlockPos> RENDER_RECURSION_GUARD = new HashSet<>();
    
    public LightRedirectorRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
        System.out.println("LightRedirectorRenderer: Constructor called - renderer created");
    }

    @Override
    public void render(LightRedirectorBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        
        Level level = blockEntity.getLevel();
        if (level == null) {
            System.out.println("LightRedirectorRenderer: Level is null");
            return;
        }
        
        BlockPos redirectorPos = blockEntity.getBlockPos();
        
        // First, render the base light redirector block with per-face textures
        renderLightRedirectorBlock(blockEntity, poseStack, bufferSource, combinedLight, combinedOverlay);
        
        // Prevent infinite rendering loops for visual redirection
        if (RENDER_RECURSION_GUARD.contains(redirectorPos)) {
            return;
        }
        
        // Render redirected blocks for each enabled face
        for (Direction direction : Direction.values()) {
            if (blockEntity.isEnabled(direction)) {
                renderRedirectedFace(level, redirectorPos, direction, poseStack, bufferSource, combinedLight, combinedOverlay);
            }
        }
    }
    
    private void renderLightRedirectorBlock(LightRedirectorBlockEntity blockEntity, PoseStack poseStack, 
                                           MultiBufferSource bufferSource, int combinedLight, 
                                           int combinedOverlay) {
        
        // Use adjacent block lighting approach (the one that works)
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        
        int finalLight = combinedLight;
        
        if (level != null && (combinedLight == 0 || combinedLight == 0x00000000)) {
            // Get lighting from adjacent positions
            int maxBlockLight = 0;
            int maxSkyLight = 0;
            
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
                BlockPos adjacentPos = pos.relative(dir);
                int adjBlockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, adjacentPos);
                int adjSkyLight = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, adjacentPos);
                
                maxBlockLight = Math.max(maxBlockLight, adjBlockLight);
                maxSkyLight = Math.max(maxSkyLight, adjSkyLight);
            }
            
            // Use the brightest adjacent lighting, reduced by 1
            int blockLight = Math.max(0, maxBlockLight - 1);
            int skyLight = Math.max(0, maxSkyLight - 1);
            
            finalLight = net.minecraft.client.renderer.LightTexture.pack(blockLight, skyLight);
        }
        
        // Get texture atlas
        var textureAtlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        
        // Get the two textures
        ResourceLocation enabledTexture = ResourceLocation.fromNamespaceAndPath("randomthings", "block/lightredirector");
        ResourceLocation disabledTexture = ResourceLocation.fromNamespaceAndPath("randomthings", "block/lightredirector_disabled");
        
        TextureAtlasSprite enabledSprite = textureAtlas.apply(enabledTexture);
        TextureAtlasSprite disabledSprite = textureAtlas.apply(disabledTexture);
        
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        
        // Render each face with the appropriate texture based on enabled state
        for (Direction direction : Direction.values()) {
            boolean isEnabled = blockEntity.isEnabled(direction);
            TextureAtlasSprite sprite = isEnabled ? enabledSprite : disabledSprite;
            renderFace(direction, matrix, vertexConsumer, sprite, finalLight, combinedOverlay);
        }
    }
    
    private void renderFace(Direction direction, Matrix4f matrix, VertexConsumer vertexConsumer, 
                           TextureAtlasSprite sprite, int combinedLight, int combinedOverlay) {
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();
        
        // Get direction normal for lighting
        var normal = direction.getNormal();
        
        switch (direction) {
            case DOWN -> {
                // Bottom face (y = 0)
                vertexConsumer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 0, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 0, 1).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 0, 1).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
            case UP -> {
                // Top face (y = 1)
                vertexConsumer.addVertex(matrix, 0, 1, 1).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 1).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 1, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
            case NORTH -> {
                // North face (z = 0)
                vertexConsumer.addVertex(matrix, 1, 0, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 1, 0).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 0).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
            case SOUTH -> {
                // South face (z = 1)
                vertexConsumer.addVertex(matrix, 0, 0, 1).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 0, 1).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 1).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 1, 1).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
            case WEST -> {
                // West face (x = 0)
                vertexConsumer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 0, 1).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 1, 1).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 0, 1, 0).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
            case EAST -> {
                // East face (x = 1)
                vertexConsumer.addVertex(matrix, 1, 0, 1).setColor(255, 255, 255, 255).setUv(minU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 0, 0).setColor(255, 255, 255, 255).setUv(maxU, maxV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 0).setColor(255, 255, 255, 255).setUv(maxU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
                vertexConsumer.addVertex(matrix, 1, 1, 1).setColor(255, 255, 255, 255).setUv(minU, minV).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(normal.getX(), normal.getY(), normal.getZ());
            }
        }
    }
    
    private void renderRedirectedFace(Level level, BlockPos redirectorPos, Direction enabledFace,
                                     PoseStack poseStack, MultiBufferSource bufferSource,
                                     int combinedLight, int combinedOverlay) {
        
        // Get the position that should be visually swapped
        BlockPos adjacentPos = redirectorPos.relative(enabledFace);
        BlockPos oppositePos = redirectorPos.relative(enabledFace.getOpposite());
        
        // Get the block states from both sides
        BlockState oppositeState = level.getBlockState(oppositePos);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        
        // The Light Redirector only works when there are blocks on BOTH sides
        // It swaps the two blocks, but doesn't work with air
        if (oppositeState.isAir() || adjacentState.isAir()) {
            return; // Need blocks on both sides to swap
        }
        
        // Skip if either side has another light redirector
        if (oppositeState.getBlock() instanceof lumien.randomthings.block.LightRedirectorBlock ||
            adjacentState.getBlock() instanceof lumien.randomthings.block.LightRedirectorBlock) {
            return;
        }
        
        // Check if the opposite side also has a redirector that would swap back
        BlockEntity oppositeEntity = level.getBlockEntity(oppositePos);
        if (oppositeEntity instanceof LightRedirectorBlockEntity oppositeRedirector) {
            // This is a two-way swap scenario - don't render to avoid conflicts
            if (oppositeRedirector.isEnabled(enabledFace.getOpposite())) {
                return; // Let each block be rendered in its own position
            }
        }
        
        // Skip rendering if it's the same block type to avoid unnecessary swapping
        if (oppositeState.getBlock() == adjacentState.getBlock()) {
            return; // No point swapping identical blocks
        }
        
        // Calculate proper lighting for the destination position (adjacentPos)
        int destinationLight = combinedLight;
        
        // Get lighting from adjacent positions around the destination
        int maxBlockLight = 0;
        int maxSkyLight = 0;
        
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            BlockPos lightCheckPos = adjacentPos.relative(dir);
            int adjBlockLight = level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, lightCheckPos);
            int adjSkyLight = level.getBrightness(net.minecraft.world.level.LightLayer.SKY, lightCheckPos);
            
            maxBlockLight = Math.max(maxBlockLight, adjBlockLight);
            maxSkyLight = Math.max(maxSkyLight, adjSkyLight);
        }
        
        // Use the brightest adjacent lighting for the destination position
        int blockLight = Math.max(0, maxBlockLight - 1);
        int skyLight = Math.max(0, maxSkyLight - 1);
        destinationLight = net.minecraft.client.renderer.LightTexture.pack(blockLight, skyLight);
        
        System.out.println("LightRedirectorRenderer: Destination lighting at " + adjacentPos + " - Block: " + blockLight + ", Sky: " + skyLight + ", Combined: " + destinationLight);
        
        // Calculate the render position offset
        double offsetX = adjacentPos.getX() - redirectorPos.getX();
        double offsetY = adjacentPos.getY() - redirectorPos.getY();
        double offsetZ = adjacentPos.getZ() - redirectorPos.getZ();
        
        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, offsetZ);
        
        // Always use the special rendering approach since we're always swapping blocks
        // (Light Redirector only works with blocks on both sides)
        renderBlockModelTranslucent(oppositeState, oppositePos, poseStack, bufferSource, destinationLight, combinedOverlay);
        
        poseStack.popPose();
    }
    
    private void renderBlockModel(BlockState state, BlockPos pos, PoseStack poseStack, 
                                 MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        
        // Get the appropriate render type for the block
        RenderType renderType = getRenderType(state);
        
        // Render the block model
        blockRenderer.renderSingleBlock(
            state,
            poseStack,
            bufferSource,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            renderType
        );
    }
    
    private void renderBlockModelTranslucent(BlockState state, BlockPos pos, PoseStack poseStack, 
                                            MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // Use custom render type with polygon offset to prevent z-fighting
        RenderType renderType = LightRedirectorRenderType.swappedBlock();
        
        blockRenderer.renderSingleBlock(
            state,
            poseStack,
            bufferSource,
            combinedLight,
            combinedOverlay,
            ModelData.EMPTY,
            renderType
        );
    }
    
    private RenderType getRenderType(BlockState state) {
        // Determine the appropriate render type based on the block's properties
        if (state.canOcclude()) {
            return RenderType.solid();
        } else {
            // For now, use cutout for non-occluding blocks
            return RenderType.cutout();
        }
    }
    
    @Override
    public boolean shouldRenderOffScreen(LightRedirectorBlockEntity blockEntity) {
        // Allow rendering even when the block entity is off-screen
        // This ensures redirected blocks are visible
        return true;
    }
    
    @Override
    public int getViewDistance() {
        // Extend view distance for better visibility
        return 128;
    }
}