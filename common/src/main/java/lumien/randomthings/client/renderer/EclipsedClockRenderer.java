package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the hung clock as its item model (time predicate drives the face) plus a floating time label. */
@Environment(EnvType.CLIENT)
public class EclipsedClockRenderer extends EntityRenderer<EclipsedClockEntity> {
    private final ItemRenderer itemRenderer;

    public EclipsedClockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EclipsedClockEntity clockEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Direction direction = clockEntity.getDirection();
        poseStack.mulPose(Axis.YP.rotationDegrees(direction.toYRot()));

        ItemStack clockStack = new ItemStack(ModItems.ECLIPSED_CLOCK.get());
        RTNbt.setInt(clockStack, RTDataKeys.TARGET_TIME, clockEntity.getTargetTime());
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        this.itemRenderer.renderStatic(clockStack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
            poseStack, buffer, clockEntity.level(), clockEntity.getId());
        poseStack.popPose();
        poseStack.popPose();

        if (clockEntity.shouldDisplayTime()) {
            poseStack.pushPose();
            poseStack.translate(direction.getStepX() * 0.3, -0.3, direction.getStepZ() * 0.3);
            this.renderNameTag(clockEntity, Component.literal(clockEntity.getStringTargetTime()), poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        super.render(clockEntity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EclipsedClockEntity entity) {
        return new ResourceLocation("minecraft", "textures/atlas/blocks.png");
    }
}
