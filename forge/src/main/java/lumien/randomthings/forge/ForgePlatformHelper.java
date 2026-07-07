package lumien.randomthings.forge;

import lumien.randomthings.platform.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public net.minecraft.world.item.BlockItem createPlantChestItem(
            net.minecraft.world.level.block.Block block, net.minecraft.world.item.Item.Properties properties) {
        return new PlantChestForgeItem(block, properties);
    }
}
