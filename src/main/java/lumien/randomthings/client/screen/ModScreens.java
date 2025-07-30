package lumien.randomthings.client.screen;

import lumien.randomthings.menu.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModScreens {
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), AdvancedRedstoneTorchScreen::new);
    }
}