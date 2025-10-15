package lumien.randomthings.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import lumien.randomthings.entity.ThrownWeatherEggEntity;
import lumien.randomthings.item.WeatherEggItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownWeatherEggRenderer extends EntityRenderer<ThrownWeatherEggEntity> {

    private final ItemRenderer itemRenderer;

    public ThrownWeatherEggRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ThrownWeatherEggEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Scale and rotate to match item appearance
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        // Render the item as a 3D model based on weather type
        WeatherEggItem.WeatherType type = entity.getWeatherType();
        ItemStack itemStack = switch (type) {
            case SUN -> new ItemStack(lumien.randomthings.item.ModItems.WEATHER_EGG_SUN.get());
            case RAIN -> new ItemStack(lumien.randomthings.item.ModItems.WEATHER_EGG_RAIN.get());
            case STORM -> new ItemStack(lumien.randomthings.item.ModItems.WEATHER_EGG_STORM.get());
        };
        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.GROUND, packedLight,
            OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownWeatherEggEntity entity) {
        // Not used since we're rendering as an item
        return null;
    }
}
