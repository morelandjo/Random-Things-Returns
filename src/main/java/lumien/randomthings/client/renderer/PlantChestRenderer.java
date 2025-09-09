package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import lumien.randomthings.blockentity.PlantChestBlockEntity;
import lumien.randomthings.block.PlantChestBlock;
import lumien.randomthings.client.model.PlantChestModel;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlantChestRenderer extends BlockEntityWithoutLevelRenderer implements BlockEntityRenderer<PlantChestBlockEntity> {
    
    public static final ModelLayerLocation PLANT_CHEST_LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "plant_chest"), "main");
    
    private static final ResourceLocation PLANT_CHEST_TEXTURE = 
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/entity/chest/plant_chest.png");
    
    private final PlantChestModel model;
    private final BlockEntityRendererProvider.Context context;
    
    public PlantChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context.getBlockEntityRenderDispatcher(), context.getModelSet());
        this.context = context;
        this.model = new PlantChestModel(context.bakeLayer(PLANT_CHEST_LAYER));
    }
    
    @Override
    public void render(PlantChestBlockEntity blockEntity, float partialTick, PoseStack poseStack, 
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        
        BlockState blockState = blockEntity.getBlockState();
        ChestType chestType = blockState.getValue(PlantChestBlock.TYPE);
        
        // Only render single chests for now
        if (chestType != ChestType.SINGLE) {
            return;
        }
        
        poseStack.pushPose();
        
        // Translate to center of block
        poseStack.translate(0.5F, 0.5F, 0.5F);
        
        // Rotate based on facing direction
        Direction facing = blockState.getValue(PlantChestBlock.FACING);
        float rotation = switch (facing) {
            case NORTH -> 180F;
            case SOUTH -> 0F;
            case WEST -> 90F;
            case EAST -> -90F;
            default -> 0F;
        };
        poseStack.mulPose(Axis.YN.rotationDegrees(rotation));
        
        // Translate back for rendering
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        
        // Get lid animation
        float lidAngle = getLidOpenness(blockEntity, partialTick);
        
        // Setup animation
        model.setupAnim(lidAngle);
        
        // Get vertex consumer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(PLANT_CHEST_TEXTURE));
        
        // Render the model
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        poseStack.popPose();
    }
    
    private float getLidOpenness(PlantChestBlockEntity chest, float partialTick) {
        // ChestBlockEntity handles lid animation
        return chest.getOpenNess(partialTick);
    }
    
    @Override
    public AABB getRenderBoundingBox(PlantChestBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).expandTowards(1.0, 1.0, 1.0);
    }
    
    @Override
    public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // Render the chest item
        poseStack.pushPose();
        
        // Get block state from item
        BlockState blockState = ModBlocks.PLANT_CHEST.get().defaultBlockState()
            .setValue(PlantChestBlock.FACING, Direction.SOUTH)
            .setValue(PlantChestBlock.TYPE, ChestType.SINGLE);
        
        // Translate to center of block
        poseStack.translate(0.5F, 0.5F, 0.5F);
        
        // Apply rotation for display context
        if (displayContext == ItemDisplayContext.GUI) {
            // Rotate for GUI display
            poseStack.mulPose(Axis.YN.rotationDegrees(180F));
        } else {
            // Standard rotation
            poseStack.mulPose(Axis.YN.rotationDegrees(180F));
        }
        
        // Translate back for rendering
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        
        // Setup animation (closed lid for item)
        model.setupAnim(0.0F);
        
        // Get vertex consumer
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutout(PLANT_CHEST_TEXTURE));
        
        // Render the model
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 0xFFFFFFFF);
        
        poseStack.popPose();
    }
}