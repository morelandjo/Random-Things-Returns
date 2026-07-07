package lumien.randomthings.fabric;

import lumien.randomthings.platform.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public net.minecraft.world.item.BlockItem createPlantChestItem(
            net.minecraft.world.level.block.Block block, net.minecraft.world.item.Item.Properties properties) {
        // The chest-model item renderer is registered client-side via BuiltinItemRendererRegistry.
        return new net.minecraft.world.item.BlockItem(block, properties);
    }
}
