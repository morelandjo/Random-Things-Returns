package lumien.randomthings.client.screen;

import lumien.randomthings.menu.ModMenuTypes;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModScreens {
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_REDSTONE_TORCH.get(), AdvancedRedstoneTorchScreen::new);
        event.register(ModMenuTypes.ADVANCED_REDSTONE_REPEATER.get(), AdvancedRedstoneRepeaterScreen::new);
        event.register(ModMenuTypes.ANALOG_EMITTER.get(), AnalogEmitterScreen::new);
        event.register(ModMenuTypes.POTION_VAPORIZER.get(), PotionVaporizerScreen::new);
        event.register(ModMenuTypes.BLOCK_DESTABILIZER.get(), BlockDestabilizerScreen::new);
        event.register(ModMenuTypes.IRON_DROPPER.get(), IronDropperScreen::new);
        event.register(ModMenuTypes.CHAT_DETECTOR.get(), ChatDetectorScreen::new);
        event.register(ModMenuTypes.GLOBAL_CHAT_DETECTOR.get(), GlobalChatDetectorScreen::new);
        event.register(ModMenuTypes.IGNITER.get(), IgniterScreen::new);
        event.register(ModMenuTypes.INVENTORY_TESTER.get(), InventoryTesterScreen::new);
        event.register(ModMenuTypes.NOTIFICATION_INTERFACE.get(), NotificationInterfaceScreen::new);
        event.register(ModMenuTypes.CHUNK_ANALYZER.get(), ChunkAnalyzerScreen::new);
        event.register(ModMenuTypes.ENDER_LETTER.get(), EnderLetterScreen::new);
        event.register(ModMenuTypes.ENDER_MAILBOX.get(), EnderMailboxScreen::new);
        event.register(ModMenuTypes.ITEM_FILTER.get(), ItemFilterScreen::new);
        event.register(ModMenuTypes.REDSTONE_REMOTE_EDIT.get(), RedstoneRemoteEditScreen::new);
        event.register(ModMenuTypes.REDSTONE_REMOTE_USE.get(), RedstoneRemoteUseScreen::new);
        event.register(ModMenuTypes.PORTABLE_SOUND_DAMPENER.get(), PortableSoundDampenerScreen::new);
        event.register(ModMenuTypes.SOUND_DAMPENER.get(), SoundDampenerScreen::new);
        event.register(ModMenuTypes.SOUND_RECORDER.get(), SoundRecorderScreen::new);
    }
}