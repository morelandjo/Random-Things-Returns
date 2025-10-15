package lumien.randomthings.client.renderer.block_entity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import lumien.randomthings.blockentity.AncientFurnaceBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class AncientFurnaceRenderer implements BlockEntityRenderer<AncientFurnaceBlockEntity> {

    private static final ResourceLocation[] OVERLAY_TEXTURES = new ResourceLocation[] {
        ResourceLocation.fromNamespaceAndPath("randomthings", "block/ancientbrick_o0"),
        ResourceLocation.fromNamespaceAndPath("randomthings", "block/ancientbrick_o1"),
        ResourceLocation.fromNamespaceAndPath("randomthings", "block/ancientbrick_o2"),
        ResourceLocation.fromNamespaceAndPath("randomthings", "block/ancientbrick_o3")
    };

    // Texture rotation patterns - which texture to use for each horizontal facing
    private static final int[][] TEXTURE_INDICES = new int[][] {
        {2, 3, 0, 1}, // Pattern 0
        {1, 2, 3, 0}, // Pattern 1
        {0, 1, 2, 3}, // Pattern 2
        {3, 0, 1, 2}  // Pattern 3
    };

    // Offset positions and their facing directions for overlay rendering
    private static class Overlay {
        int offsetX, offsetZ;
        Direction[] facings;

        Overlay(int x, int z, Direction... facings) {
            this.offsetX = x;
            this.offsetZ = z;
            this.facings = facings;
        }
    }

    private static final List<Overlay> OVERLAYS = new ArrayList<>();

    static {
        // Cardinal directions
        OVERLAYS.add(new Overlay(0, -1, Direction.NORTH));
        OVERLAYS.add(new Overlay(0, 1, Direction.SOUTH));
        OVERLAYS.add(new Overlay(1, 0, Direction.EAST));
        OVERLAYS.add(new Overlay(-1, 0, Direction.WEST));

        // Diagonals
        OVERLAYS.add(new Overlay(1, -1, Direction.NORTH, Direction.EAST));
        OVERLAYS.add(new Overlay(1, 1, Direction.SOUTH, Direction.EAST));
        OVERLAYS.add(new Overlay(-1, 1, Direction.SOUTH, Direction.WEST));
        OVERLAYS.add(new Overlay(-1, -1, Direction.NORTH, Direction.WEST));
    }

    public AncientFurnaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AncientFurnaceBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        AncientFurnaceBlockEntity.State state = blockEntity.getState();
        int counter = blockEntity.getStartingCounter();
        float transparency = 0;

        if (state == AncientFurnaceBlockEntity.State.RUNNING) {
            transparency = 1.0f;
        } else if (state == AncientFurnaceBlockEntity.State.STARTING && counter > 100) {
            // Fade in after warmup reaches 100 ticks (5 seconds)
            float fadeProgress = (counter + partialTick - 100) / 300.0f;
            transparency = Math.min(1.0f, -1.0f * (float) Math.cos(fadeProgress * (Math.PI / 2)) + 1.0f);
        }

        if (transparency <= 0) {
            return;
        }

        // Animated yellow-orange glow
        long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0;
        float yellowIntensity = ((float) Math.sin((gameTime + partialTick) / 25f) + 1) / 5f + 0.1f;
        float red = 1.0f;
        float green = yellowIntensity;
        float blue = 0.0f;
        float alpha = transparency;

        BlockPos furnacePos = blockEntity.getBlockPos();

        // Render overlays on surrounding blocks
        for (Overlay overlay : OVERLAYS) {
            BlockPos overlayPos = furnacePos.offset(overlay.offsetX, 0, overlay.offsetZ);

            // Deterministic random based on position
            long seed = Mth.getSeed(overlayPos);
            int patternIndex = Math.abs((int) (seed >> 16) % 4);

            for (Direction facing : overlay.facings) {
                int textureIndex = TEXTURE_INDICES[patternIndex][facing.get2DDataValue()];
                ResourceLocation textureLocation = OVERLAY_TEXTURES[textureIndex];

                TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(textureLocation);

                poseStack.pushPose();

                // Translate to the overlay block position relative to furnace
                poseStack.translate(overlay.offsetX, 0, overlay.offsetZ);

                // Render the overlay on the specified face
                renderOverlayFace(poseStack, bufferSource, facing, sprite, red, green, blue, alpha, 0xF000F0); // Full bright

                poseStack.popPose();
            }
        }
    }

    private void renderOverlayFace(PoseStack poseStack, MultiBufferSource bufferSource, Direction facing,
                                  TextureAtlasSprite sprite, float red, float green, float blue, float alpha,
                                  int packedLight) {

        VertexConsumer builder = bufferSource.getBuffer(RenderType.translucent());
        var matrix = poseStack.last().pose();

        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        // Slight offset from block face to prevent z-fighting
        float offset = 0.001f;

        switch (facing) {
            case NORTH:
                builder.addVertex(matrix, 0, 0, -offset).setColor(red, green, blue, alpha).setUv(maxU, maxV).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 0, 1, -offset).setColor(red, green, blue, alpha).setUv(maxU, minV).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 1, 1, -offset).setColor(red, green, blue, alpha).setUv(minU, minV).setLight(packedLight).setNormal(0, 0, -1);
                builder.addVertex(matrix, 1, 0, -offset).setColor(red, green, blue, alpha).setUv(minU, maxV).setLight(packedLight).setNormal(0, 0, -1);
                break;

            case SOUTH:
                builder.addVertex(matrix, 0, 0, 1 + offset).setColor(red, green, blue, alpha).setUv(minU, maxV).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 1, 0, 1 + offset).setColor(red, green, blue, alpha).setUv(maxU, maxV).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 1, 1, 1 + offset).setColor(red, green, blue, alpha).setUv(maxU, minV).setLight(packedLight).setNormal(0, 0, 1);
                builder.addVertex(matrix, 0, 1, 1 + offset).setColor(red, green, blue, alpha).setUv(minU, minV).setLight(packedLight).setNormal(0, 0, 1);
                break;

            case WEST:
                builder.addVertex(matrix, -offset, 0, 0).setColor(red, green, blue, alpha).setUv(minU, maxV).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, -offset, 0, 1).setColor(red, green, blue, alpha).setUv(maxU, maxV).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, -offset, 1, 1).setColor(red, green, blue, alpha).setUv(maxU, minV).setLight(packedLight).setNormal(-1, 0, 0);
                builder.addVertex(matrix, -offset, 1, 0).setColor(red, green, blue, alpha).setUv(minU, minV).setLight(packedLight).setNormal(-1, 0, 0);
                break;

            case EAST:
                builder.addVertex(matrix, 1 + offset, 0, 0).setColor(red, green, blue, alpha).setUv(maxU, maxV).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1 + offset, 1, 0).setColor(red, green, blue, alpha).setUv(maxU, minV).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1 + offset, 1, 1).setColor(red, green, blue, alpha).setUv(minU, minV).setLight(packedLight).setNormal(1, 0, 0);
                builder.addVertex(matrix, 1 + offset, 0, 1).setColor(red, green, blue, alpha).setUv(minU, maxV).setLight(packedLight).setNormal(1, 0, 0);
                break;

            default:
                break;
        }
    }

    @Override
    public boolean shouldRenderOffScreen(AncientFurnaceBlockEntity blockEntity) {
        return true; // Render overlays even if furnace itself is off-screen
    }

    @Override
    public int getViewDistance() {
        return 256; // Render from far away
    }
}
