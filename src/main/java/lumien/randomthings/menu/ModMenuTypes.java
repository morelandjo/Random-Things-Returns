package lumien.randomthings.menu;

import lumien.randomthings.blockentity.BlockDestabilizerBlockEntity;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(BuiltInRegistries.MENU, ModConstants.MOD_ID);

    public static final Supplier<MenuType<AdvancedRedstoneTorchMenu>> ADVANCED_REDSTONE_TORCH = 
        MENU_TYPES.register("advanced_redstone_torch", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new AdvancedRedstoneTorchMenu(windowId, ContainerLevelAccess.NULL)));

    public static final Supplier<MenuType<AdvancedRedstoneRepeaterMenu>> ADVANCED_REDSTONE_REPEATER = 
        MENU_TYPES.register("advanced_redstone_repeater", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new AdvancedRedstoneRepeaterMenu(windowId, ContainerLevelAccess.NULL)));

    public static final Supplier<MenuType<AnalogEmitterMenu>> ANALOG_EMITTER = 
        MENU_TYPES.register("analog_emitter", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new AnalogEmitterMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<PotionVaporizerMenu>> POTION_VAPORIZER = 
        MENU_TYPES.register("potion_vaporizer", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new PotionVaporizerMenu(windowId, inventory, data.readBlockPos())));

    // public static final Supplier<MenuType<OnlineDetectorMenu>> ONLINE_DETECTOR = 
    //     MENU_TYPES.register("online_detector", 
    //         () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
    //             new OnlineDetectorMenu(windowId)));

    public static final Supplier<MenuType<BlockDestabilizerMenu>> BLOCK_DESTABILIZER = 
        MENU_TYPES.register("block_destabilizer", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                BlockDestabilizerBlockEntity blockEntity = (BlockDestabilizerBlockEntity) inventory.player.level()
                    .getBlockEntity(data.readBlockPos());
                return new BlockDestabilizerMenu(windowId, blockEntity);
            }));

    public static final Supplier<MenuType<IronDropperMenu>> IRON_DROPPER = 
        MENU_TYPES.register("iron_dropper", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                var pos = data.readBlockPos();
                var blockEntity = (lumien.randomthings.blockentity.IronDropperBlockEntity) inventory.player.level()
                    .getBlockEntity(pos);
                return new IronDropperMenu(windowId, inventory, blockEntity, pos);
            }));

    public static final Supplier<MenuType<ChatDetectorMenu>> CHAT_DETECTOR = 
        MENU_TYPES.register("chat_detector", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new ChatDetectorMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<GlobalChatDetectorMenu>> GLOBAL_CHAT_DETECTOR = 
        MENU_TYPES.register("global_chat_detector", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new GlobalChatDetectorMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<IgniterMenu>> IGNITER = 
        MENU_TYPES.register("igniter", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new IgniterMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<InventoryTesterMenu>> INVENTORY_TESTER = 
        MENU_TYPES.register("inventory_tester", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new InventoryTesterMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<NotificationInterfaceMenu>> NOTIFICATION_INTERFACE = 
        MENU_TYPES.register("notification_interface", 
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> 
                new NotificationInterfaceMenu(windowId, inventory, ContainerLevelAccess.create(inventory.player.level(), data.readBlockPos()))));

    public static final Supplier<MenuType<ChunkAnalyzerMenu>> CHUNK_ANALYZER =
        MENU_TYPES.register("chunk_analyzer",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                // For item-based menus, we need to get the held item
                var heldItem = inventory.player.getMainHandItem();
                return new ChunkAnalyzerMenu(windowId, inventory, heldItem);
            }));

    public static final Supplier<MenuType<EnderLetterMenu>> ENDER_LETTER =
        MENU_TYPES.register("ender_letter",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                // For item-based menus, we need to get the held item
                var heldItem = inventory.player.getMainHandItem();
                return new EnderLetterMenu(windowId, inventory, heldItem);
            }));

    public static final Supplier<MenuType<EnderMailboxMenu>> ENDER_MAILBOX =
        MENU_TYPES.register("ender_mailbox",
            () -> IMenuTypeExtension.create((windowId, inventory, data) ->
                new EnderMailboxMenu(windowId, inventory, data.readBlockPos())));

    public static final Supplier<MenuType<ItemFilterMenu>> ITEM_FILTER =
        MENU_TYPES.register("item_filter",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                // For item-based menus, we need to get the held item
                var heldItem = inventory.player.getMainHandItem();
                return new ItemFilterMenu(windowId, inventory, heldItem);
            }));

    public static final Supplier<MenuType<RedstoneRemoteEditMenu>> REDSTONE_REMOTE_EDIT =
        MENU_TYPES.register("redstone_remote_edit",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                var hand = data.readEnum(net.minecraft.world.InteractionHand.class);
                return new RedstoneRemoteEditMenu(windowId, inventory, hand);
            }));

    public static final Supplier<MenuType<RedstoneRemoteUseMenu>> REDSTONE_REMOTE_USE =
        MENU_TYPES.register("redstone_remote_use",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                var hand = data.readEnum(net.minecraft.world.InteractionHand.class);
                return new RedstoneRemoteUseMenu(windowId, inventory, hand);
            }));

    public static final Supplier<MenuType<PortableSoundDampenerMenu>> PORTABLE_SOUND_DAMPENER =
        MENU_TYPES.register("portable_sound_dampener",
            () -> IMenuTypeExtension.create((windowId, inventory, data) -> {
                var hand = data.readEnum(net.minecraft.world.InteractionHand.class);
                return new PortableSoundDampenerMenu(windowId, inventory, hand);
            }));

    public static final Supplier<MenuType<SoundDampenerMenu>> SOUND_DAMPENER =
        MENU_TYPES.register("sound_dampener",
            () -> IMenuTypeExtension.create((windowId, inventory, data) ->
                new SoundDampenerMenu(windowId, inventory, data.readBlockPos())));

}