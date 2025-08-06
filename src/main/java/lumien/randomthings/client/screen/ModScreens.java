package lumien.randomthings.client.screen;

import lumien.randomthings.menu.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModScreens {
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), AdvancedRedstoneTorchScreen::new);
        event.register(ModMenuTypes.ADVANCED_REDSTONE_REPEATER.get(), AdvancedRedstoneRepeaterScreen::new);
        event.register(ModMenuTypes.ANALOG_EMITTER.get(), AnalogEmitterScreen::new);
        event.register(ModMenuTypes.BLOCK_DESTABILIZER.get(), BlockDestabilizerScreen::new);
        event.register(ModMenuTypes.CHAT_DETECTOR.get(), ChatDetectorScreen::new);
        event.register(ModMenuTypes.GLOBAL_CHAT_DETECTOR.get(), GlobalChatDetectorScreen::new);
    }
}