package lumien.randomthings.forge;

import dev.architectury.platform.forge.EventBuses;
import lumien.randomthings.RandomThings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Forge main entry point.
 */
@Mod(RandomThings.MOD_ID)
public class RandomThingsForge {

    public RandomThingsForge() {
        // Register the mod event bus with Architectury before any registry calls.
        EventBuses.registerModEventBus(RandomThings.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        RandomThings.init();

        // Client-only setup (screens, renderers). Kept in a separate class so the dedicated server
        // never loads client code.
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeClientSetup::init);
    }
}
