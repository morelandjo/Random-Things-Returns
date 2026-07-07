package lumien.randomthings.menu;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;

/**
 * Menu (container) type registry.
 *
 * <p>GUI blocks that pass extra data (e.g. the block position) to the client use
 * {@link MenuRegistry#ofExtended} — the server writes a {@code FriendlyByteBuf} via the block
 * entity's {@code ExtendedMenuProvider#saveExtraData}, and the registered factory rebuilds the menu
 * from that buffer on the client. This is the cross-loader replacement for NeoForge's
 * {@code openMenu(provider, pos)}.</p>
 */
public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<OnlineDetectorMenu>> ONLINE_DETECTOR =
        MENU_TYPES.register("online_detector",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new OnlineDetectorMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<EntityDetectorMenu>> ENTITY_DETECTOR =
        MENU_TYPES.register("entity_detector",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new EntityDetectorMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<ChatDetectorMenu>> CHAT_DETECTOR =
        MENU_TYPES.register("chat_detector",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new ChatDetectorMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<GlobalChatDetectorMenu>> GLOBAL_CHAT_DETECTOR =
        MENU_TYPES.register("global_chat_detector",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new GlobalChatDetectorMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<AdvancedItemCollectorMenu>> ADVANCED_ITEM_COLLECTOR =
        MENU_TYPES.register("advanced_item_collector",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new AdvancedItemCollectorMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<ItemFilterMenu>> ITEM_FILTER =
        MENU_TYPES.register("item_filter",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new ItemFilterMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<DyeingMachineMenu>> DYEING_MACHINE =
        MENU_TYPES.register("dyeing_machine",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new DyeingMachineMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<ImbuingStationMenu>> IMBUING_STATION =
        MENU_TYPES.register("imbuing_station",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new ImbuingStationMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<AnalogEmitterMenu>> ANALOG_EMITTER =
        MENU_TYPES.register("analog_emitter",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new AnalogEmitterMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<AdvancedRedstoneTorchMenu>> ADVANCED_REDSTONE_TORCH =
        MENU_TYPES.register("advanced_redstone_torch",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new AdvancedRedstoneTorchMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<AdvancedRedstoneRepeaterMenu>> ADVANCED_REDSTONE_REPEATER =
        MENU_TYPES.register("advanced_redstone_repeater",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new AdvancedRedstoneRepeaterMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<IgniterMenu>> IGNITER =
        MENU_TYPES.register("igniter",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new IgniterMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<IronDropperMenu>> IRON_DROPPER =
        MENU_TYPES.register("iron_dropper",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new IronDropperMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<ChunkAnalyzerMenu>> CHUNK_ANALYZER =
        MENU_TYPES.register("chunk_analyzer",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new ChunkAnalyzerMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<SoundDampenerMenu>> SOUND_DAMPENER =
        MENU_TYPES.register("sound_dampener",
            () -> MenuRegistry.ofExtended((syncId, inventory, buf) ->
                new SoundDampenerMenu(syncId, inventory, buf)));

    public static final RegistrySupplier<MenuType<PortableSoundDampenerMenu>> PORTABLE_SOUND_DAMPENER =
        MENU_TYPES.register("portable_sound_dampener",
            () -> MenuRegistry.of((syncId, inventory) ->
                new PortableSoundDampenerMenu(syncId, inventory, inventory.player.getMainHandItem())));

    public static final RegistrySupplier<MenuType<SoundRecorderMenu>> SOUND_RECORDER =
        MENU_TYPES.register("sound_recorder",
            () -> MenuRegistry.of((syncId, inventory) ->
                new SoundRecorderMenu(syncId, inventory, inventory.player.getMainHandItem())));

    private ModMenuTypes() {
    }

    public static void register() {
        MENU_TYPES.register();
    }
}
