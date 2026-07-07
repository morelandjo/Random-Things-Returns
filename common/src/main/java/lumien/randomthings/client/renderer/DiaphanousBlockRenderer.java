package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.blockentity.DiaphanousBlockEntity;
import lumien.randomthings.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Renders the disguise block with a distance-based fade (fully visible far away, gone up close). */
@Environment(EnvType.CLIENT)
public class DiaphanousBlockRenderer implements BlockEntityRenderer<DiaphanousBlockEntity> {

    public DiaphanousBlockRenderer(BlockEntityRendererProvider.Context context) {
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
        double dX = pos.getX() + 0.5 - player.getX();
        double dY = pos.getY() + 0.5 - player.getY();
        double dZ = pos.getZ() + 0.5 - player.getZ();
        float alpha = calculateAlpha(blockEntity, Math.sqrt(dX * dX + dY * dY + dZ * dZ));
        if (alpha <= 0.05f) {
            return;
        }
        renderCubeWithAlpha(poseStack, bufferSource.getBuffer(RenderType.translucent()), displayState, alpha, packedLight, packedOverlay);
    }

    @Override
    public boolean shouldRenderOffScreen(DiaphanousBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private float calculateAlpha(DiaphanousBlockEntity blockEntity, double distance) {
        // Fully opaque beyond 11 blocks, fading out from 3 blocks in (reversed when inverted).
        double distanceForCalc = Math.max(0, distance - 3);
        float cosValue = (float) Math.cos(Math.PI * Math.min(distanceForCalc, 8) / 8);
        float alpha = -0.5f * (cosValue - 1);
        if (blockEntity.isInverted()) {
            alpha = 1.0f - alpha;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack held = player.getItemInHand(hand);
                if (!held.isEmpty() && held.getItem() == ModItems.DIAPHANOUS_BLOCK.get()) {
                    alpha = 0.8f;
                    break;
                }
            }
        }
        return Math.max(0.05f, Math.min(1.0f, alpha));
    }

    private void renderCubeWithAlpha(PoseStack poseStack, VertexConsumer consumer, BlockState displayState,
                                     float alpha, int packedLight, int packedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModel(displayState).getParticleIcon();
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int alphaInt = (int) (alpha * 255);
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // south (Z=1)
        quad(consumer, matrix, normal, new float[][]{{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, 0, 0, 1);
        // north (Z=0)
        quad(consumer, matrix, normal, new float[][]{{1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, 0, 0, -1);
        // up (Y=1)
        quad(consumer, matrix, normal, new float[][]{{0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, 0, 1, 0);
        // down (Y=0)
        quad(consumer, matrix, normal, new float[][]{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, 0, -1, 0);
        // east (X=1)
        quad(consumer, matrix, normal, new float[][]{{1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, 1, 0, 0);
        // west (X=0)
        quad(consumer, matrix, normal, new float[][]{{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}}, u0, v1, u1, v0, alphaInt, packedLight, packedOverlay, -1, 0, 0);
    }

    private void quad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float[][] corners,
                      float u0, float v1, float u1, float v0,
                      int alpha, int packedLight, int packedOverlay, float nx, float ny, float nz) {
        float[][] uvs = {{u0, v1}, {u1, v1}, {u1, v0}, {u0, v0}};
        for (int i = 0; i < 4; i++) {
            consumer.vertex(matrix, corners[i][0], corners[i][1], corners[i][2])
                .color(255, 255, 255, alpha)
                .uv(uvs[i][0], uvs[i][1])
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, nx, ny, nz)
                .endVertex();
        }
    }
}
