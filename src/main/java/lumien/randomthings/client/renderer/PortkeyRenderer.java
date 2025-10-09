package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import lumien.randomthings.item.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PortkeyRenderer extends BlockEntityWithoutLevelRenderer {

    public PortkeyRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        // Check if the portkey has a camo item stored
        net.minecraft.resources.ResourceLocation camoItemId = stack.get(ModDataComponents.PORTKEY_CAMO.get());

        if (camoItemId != null) {
            // Get the Item from the registry and create an ItemStack to render
            net.minecraft.world.item.Item camoItem =
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(camoItemId);

            if (camoItem != null && camoItem != net.minecraft.world.item.Items.AIR) {
                // Render the camo item with its own model
                ItemStack camoStack = new ItemStack(camoItem);

                net.minecraft.client.resources.model.BakedModel camoModel =
                    Minecraft.getInstance().getItemRenderer().getModel(camoStack, Minecraft.getInstance().level, null, 0);

                Minecraft.getInstance().getItemRenderer().render(
                    camoStack,
                    displayContext,
                    false,
                    poseStack,
                    buffer,
                    combinedLight,
                    combinedOverlay,
                    camoModel
                );
                return;
            }
        }

        // No camo - render using the portkey_base model
        net.minecraft.client.resources.model.ModelResourceLocation portkeyBaseLocation =
            new net.minecraft.client.resources.model.ModelResourceLocation(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("randomthings", "portkey_base"),
                "inventory"
            );

        net.minecraft.client.resources.model.BakedModel portkeyBaseModel =
            Minecraft.getInstance().getModelManager().getModel(portkeyBaseLocation);

        Minecraft.getInstance().getItemRenderer().render(
            stack,
            displayContext,
            false,
            poseStack,
            buffer,
            combinedLight,
            combinedOverlay,
            portkeyBaseModel
        );
    }
}
