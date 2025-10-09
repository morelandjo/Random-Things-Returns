package lumien.randomthings.client.renderer;

import lumien.randomthings.item.ModDataComponents;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PortkeyItemRenderer {

    private static PortkeyRenderer renderer;

    public static void setRenderer(PortkeyRenderer r) {
        renderer = r;
    }

    public static final IClientItemExtensions INSTANCE = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer;
        }
    };
}
