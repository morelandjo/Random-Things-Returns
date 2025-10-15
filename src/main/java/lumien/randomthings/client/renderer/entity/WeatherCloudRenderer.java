package lumien.randomthings.client.renderer.entity;

import lumien.randomthings.entity.WeatherCloudEntity;
import lumien.randomthings.item.WeatherEggItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.PoseStack;

public class WeatherCloudRenderer extends EntityRenderer<WeatherCloudEntity> {

    private static int clientAnimationCounter = 0;

    public WeatherCloudRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public static void incrementClientCounter() {
        clientAnimationCounter++;
    }

    @Override
    public void render(WeatherCloudEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        // Weather clouds are rendered via particles spawned in WeatherCloudEntity.spawnParticles()
        // No custom entity rendering needed - the particles handle all visual effects

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WeatherCloudEntity entity) {
        return null;
    }
}
