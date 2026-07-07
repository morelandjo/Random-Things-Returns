package lumien.randomthings.forge;

import com.mojang.blaze3d.vertex.PoseStack;

import lumien.randomthings.client.renderer.PlantChestRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlantChestForgeItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static PlantChestForgeItemRenderer instance;

    private PlantChestForgeItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    public static PlantChestForgeItemRenderer get() {
        if (instance == null) {
            instance = new PlantChestForgeItemRenderer();
        }
        return instance;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PlantChestRenderer.renderItemModel(poseStack, bufferSource, packedLight, packedOverlay);
    }
}
