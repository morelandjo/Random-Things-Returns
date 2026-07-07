package lumien.randomthings.forge;

import lumien.randomthings.RandomThings;
import lumien.randomthings.client.RandomThingsClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge client setup. Screen registration must run on the main thread, so it is enqueued.
 */
@Mod.EventBusSubscriber(modid = RandomThings.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeClientSetup {

    private ForgeClientSetup() {
    }

    public static void init() {
        // Referenced from RandomThingsForge via DistExecutor to force class-load only on the client.
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(RandomThingsClient::init);
    }
}
