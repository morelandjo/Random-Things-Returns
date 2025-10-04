package lumien.randomthings.client.renderer.entity;

import lumien.randomthings.entity.GoldenChickenEntity;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GoldenChickenRenderer extends MobRenderer<GoldenChickenEntity, ChickenModel<GoldenChickenEntity>> {

    private static final ResourceLocation GOLDEN_CHICKEN_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/entity/golden_chicken.png");

    public GoldenChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenChickenEntity entity) {
        return GOLDEN_CHICKEN_TEXTURE;
    }

    @Override
    protected float getBob(GoldenChickenEntity chicken, float partialTicks) {
        float f = Mth.lerp(partialTicks, chicken.oFlap, chicken.flap);
        float f1 = Mth.lerp(partialTicks, chicken.oFlapSpeed, chicken.flapSpeed);
        return (Mth.sin(f) + 1.0F) * f1;
    }
}
