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
}