package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import lumien.randomthings.block.PlantChestBlock;
import lumien.randomthings.blockentity.PlantChestBlockEntity;
import lumien.randomthings.client.model.PlantChestModel;
import lumien.randomthings.lib.ModConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

@Environment(EnvType.CLIENT)
public class PlantChestRenderer implements BlockEntityRenderer<PlantChestBlockEntity> {

    public static final ModelLayerLocation PLANT_CHEST_LAYER = new ModelLayerLocation(
        new ResourceLocation(ModConstants.MOD_ID, "plant_chest"), "main");

    private static final ResourceLocation PLANT_CHEST_TEXTURE =
        new ResourceLocation(ModConstants.MOD_ID, "textures/entity/chest/plant_chest.png");

    /** Lazily-built model for item rendering (used by the per-loader item renderers). */
    private static PlantChestModel itemModel;

    private final PlantChestModel model;

    public PlantChestRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlantChestModel(context.bakeLayer(PLANT_CHEST_LAYER));
    }

    @Override
    public void render(PlantChestBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState blockState = blockEntity.getBlockState();
        if (blockState.getValue(PlantChestBlock.TYPE) != ChestType.SINGLE) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);

        Direction facing = blockState.getValue(PlantChestBlock.FACING);
        float rotation = switch (facing) {
            case NORTH -> 180F;
            case SOUTH -> 0F;
            case WEST -> 90F;
            case EAST -> -90F;
            default -> 0F;
        };
        poseStack.mulPose(Axis.YN.rotationDegrees(rotation));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        model.setupAnim(blockEntity.getOpenNess(partialTick));

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(PLANT_CHEST_TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }

    /** Renders the closed chest for the item form; called from the per-loader item renderers. */
    public static void renderItemModel(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (itemModel == null) {
            itemModel = new PlantChestModel(Minecraft.getInstance().getEntityModels().bakeLayer(PLANT_CHEST_LAYER));
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YN.rotationDegrees(180F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        itemModel.setupAnim(0.0F);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(PLANT_CHEST_TEXTURE));
        itemModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}
