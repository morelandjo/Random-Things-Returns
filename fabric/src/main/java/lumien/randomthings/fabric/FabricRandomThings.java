package lumien.randomthings.fabric;

import lumien.randomthings.RandomThings;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric main entry point.
 */
public class FabricRandomThings implements ModInitializer {

    @Override
    public void onInitialize() {
        RandomThings.init();
        // Optional Team Reborn Energy integration (no-op when the API isn't present).
        FabricEnergyBridge.init();
    }
}
