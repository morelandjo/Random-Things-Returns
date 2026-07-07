package lumien.randomthings.fabric;

import lumien.randomthings.client.RandomThingsClient;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entry point.
 */
public class FabricRandomThingsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RandomThingsClient.init();
        net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.INSTANCE.register(
            lumien.randomthings.item.ModItems.PLANT_CHEST.get(),
            (stack, mode, poseStack, bufferSource, packedLight, packedOverlay) ->
                lumien.randomthings.client.renderer.PlantChestRenderer.renderItemModel(poseStack, bufferSource, packedLight, packedOverlay));
    }
}
