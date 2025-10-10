package lumien.randomthings.client.renderer.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lumien.randomthings.blockentity.BlockEntityRuneBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.DyeColor;
import org.joml.Matrix4f;

public class RuneBaseBlockEntityRenderer implements BlockEntityRenderer<BlockEntityRuneBase> {

    private static final ResourceLocation RUNE_TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "block/runebaseflat");
    private static final float DUST_SIZE = 0.125f; // 2/16 blocks (2 pixels)
    private static final float Y_OFFSET = 0.0125f; // Slightly above the block surface

    public RuneBaseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntityRuneBase blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        int[][] runeData = blockEntity.getRuneData();
        if (runeData == null) {
            return;
        }

        // Get the texture sprite
        TextureAtlasSprite sprite = Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(RUNE_TEXTURE);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.cutout());

        poseStack.pushPose();

        // Render each dust particle in the 4x4 grid
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 4; z++) {
                int colorId = runeData[x][z];
                if (colorId >= 0 && colorId <= 15) {
                    // Calculate position in the grid
                    float xPos = x * 0.25f;
                    float zPos = z * 0.25f;

                    // Get the dye color
                    DyeColor dyeColor = DyeColor.byId(colorId);
                    int color = dyeColor.getFireworkColor();

                    // Extract RGB components
                    float red = ((color >> 16) & 0xFF) / 255.0f;
                    float green = ((color >> 8) & 0xFF) / 255.0f;
                    float blue = (color & 0xFF) / 255.0f;

                    // Render a small quad for this dust particle
                    renderDustQuad(poseStack, vertexConsumer, sprite,
                        xPos, Y_OFFSET, zPos,
                        DUST_SIZE, DUST_SIZE,
                        red, green, blue,
                        packedLight, packedOverlay);
                }
            }
        }

        poseStack.popPose();
    }

    private void renderDustQuad(PoseStack poseStack, VertexConsumer vertexConsumer, TextureAtlasSprite sprite,
                               float x, float y, float z, float width, float depth,
                               float red, float green, float blue,
                               int packedLight, int packedOverlay) {

        Matrix4f matrix = poseStack.last().pose();

        // Get UV coordinates from sprite
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // We want to use a small portion of the texture for each dust particle
        // Scale UV to use only center portion
        float uRange = maxU - minU;
        float vRange = maxV - minV;
        float uCenter = minU + uRange * 0.5f;
        float vCenter = minV + vRange * 0.5f;
        float uSize = uRange * 0.25f;
        float vSize = vRange * 0.25f;

        float u0 = uCenter - uSize;
        float u1 = uCenter + uSize;
        float v0 = vCenter - vSize;
        float v1 = vCenter + vSize;

        // Render quad (horizontal surface)
        // Vertex order: bottom-left, bottom-right, top-right, top-left
        vertexConsumer.addVertex(matrix, x, y, z + depth)
            .setColor(red, green, blue, 1.0f)
            .setUv(u0, v1)
            .setOverlay(packedOverlay)
            .setLight(packedLight)
            .setNormal(0, 1, 0);

        vertexConsumer.addVertex(matrix, x + width, y, z + depth)
            .setColor(red, green, blue, 1.0f)
            .setUv(u1, v1)
            .setOverlay(packedOverlay)
            .setLight(packedLight)
            .setNormal(0, 1, 0);

        vertexConsumer.addVertex(matrix, x + width, y, z)
            .setColor(red, green, blue, 1.0f)
            .setUv(u1, v0)
            .setOverlay(packedOverlay)
            .setLight(packedLight)
            .setNormal(0, 1, 0);

        vertexConsumer.addVertex(matrix, x, y, z)
            .setColor(red, green, blue, 1.0f)
            .setUv(u0, v0)
            .setOverlay(packedOverlay)
            .setLight(packedLight)
            .setNormal(0, 1, 0);
    }
}
