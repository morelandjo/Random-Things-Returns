package lumien.randomthings.client.renderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlantChestItemRenderer {
    
    private static PlantChestRenderer renderer;
    
    public static void setRenderer(PlantChestRenderer r) {
        renderer = r;
    }
    
    public static final IClientItemExtensions INSTANCE = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    };
}