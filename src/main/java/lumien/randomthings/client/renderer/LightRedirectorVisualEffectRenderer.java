package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.blockentity.LightRedirectorBlockEntity;
// import lumien.randomthings.client.handler.LightRedirectorClientHandler; // Removed
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Block entity renderer for Light Redirector that handles the visual redirection effects.
 * This renderer draws the redirected blocks at their visual positions.
 */
public class LightRedirectorVisualEffectRenderer implements BlockEntityRenderer<LightRedirectorBlockEntity> {
    
    private final BlockRenderDispatcher blockRenderer;
    
    public LightRedirectorVisualEffectRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }
    
    @Override
    public void render(LightRedirectorBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        Level level = blockEntity.getLevel();
        if (level == null || !blockEntity.isEstablished()) {
            return;
        }
        
        BlockPos redirectorPos = blockEntity.getBlockPos();
        
        // Render redirected blocks for each enabled direction
        for (Direction direction : Direction.values()) {
            if (blockEntity.isEnabled(direction)) {
                renderRedirectedBlock(blockEntity, direction, poseStack, bufferSource, packedLight, packedOverlay);
            }
        }
    }
    
    /**
     * Render a redirected block for a specific direction.
     */
    private void renderRedirectedBlock(LightRedirectorBlockEntity blockEntity, Direction direction,
                                     PoseStack poseStack, MultiBufferSource bufferSource, 
                                     int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        
        BlockPos redirectorPos = blockEntity.getBlockPos();
        BlockPos adjacentPos = redirectorPos.relative(direction);
        BlockPos oppositePos = redirectorPos.relative(direction.getOpposite());
        
        // Get the block that should be shown at the adjacent position
        BlockState oppositeState = level.getBlockState(oppositePos);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        
        // Don't render if the target block is air or if it's the same as what's already there
        if (oppositeState.isAir() || oppositeState.equals(adjacentState)) {
            return;
        }
        
        // Client handler removed - no longer using texture swapping approach
        
        poseStack.pushPose();
        
        // Translate to the adjacent block position
        poseStack.translate(
            direction.getStepX(),
            direction.getStepY(), 
            direction.getStepZ()
        );
        
        try {
            // Render the opposite block at the adjacent position
            renderBlockState(oppositeState, poseStack, bufferSource, packedLight, packedOverlay);
        } catch (Exception e) {
            // Catch any rendering errors to prevent crashes
            System.err.println("Error rendering redirected block: " + e.getMessage());
        } finally {
            poseStack.popPose();
        }
    }
    
    /**
     * Render a specific block state.
     */
    private void renderBlockState(BlockState blockState, PoseStack poseStack, 
                                MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        // Use solid render type for simplicity
        RenderType renderType = RenderType.solid();
        
        // Render the block model
        blockRenderer.renderSingleBlock(
            blockState,
            poseStack,
            bufferSource,
            packedLight,
            packedOverlay,
            ModelData.EMPTY,
            renderType
        );
    }
    
    @Override
    public boolean shouldRenderOffScreen(LightRedirectorBlockEntity blockEntity) {
        // Allow off-screen rendering for blocks that might be redirected
        return true;
    }
    
    @Override
    public int getViewDistance() {
        // Use a reasonable view distance for the visual effects
        return 64;
    }
}