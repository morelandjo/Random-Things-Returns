package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = 
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModConstants.MOD_ID);

    public static final Supplier<BlockEntityType<AdvancedRedstoneTorchBlockEntity>> ADVANCED_REDSTONE_TORCH = 
        BLOCK_ENTITY_TYPES.register("advanced_redstone_torch", 
            () -> BlockEntityType.Builder.of(AdvancedRedstoneTorchBlockEntity::new, 
                ModBlocks.ADVANCED_REDSTONE_TORCH.get(), ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get()).build(null));

    public static final Supplier<BlockEntityType<RainShieldBlockEntity>> RAIN_SHIELD = 
        BLOCK_ENTITY_TYPES.register("rain_shield", 
            () -> BlockEntityType.Builder.of(RainShieldBlockEntity::new, 
                ModBlocks.RAIN_SHIELD.get()).build(null));
                
    public static final Supplier<BlockEntityType<SlimeCubeBlockEntity>> SLIME_CUBE = 
        BLOCK_ENTITY_TYPES.register("slime_cube", 
            () -> BlockEntityType.Builder.of(SlimeCubeBlockEntity::new, 
                ModBlocks.SLIME_CUBE.get()).build(null));

    public static final Supplier<BlockEntityType<BloodRoseBlockEntity>> BLOOD_ROSE = 
        BLOCK_ENTITY_TYPES.register("blood_rose", 
            () -> BlockEntityType.Builder.of(BloodRoseBlockEntity::new, 
                ModBlocks.BLOOD_ROSE.get()).build(null));

    public static final Supplier<BlockEntityType<AdvancedRedstoneRepeaterBlockEntity>> ADVANCED_REDSTONE_REPEATER = 
        BLOCK_ENTITY_TYPES.register("advanced_redstone_repeater", 
            () -> BlockEntityType.Builder.of(AdvancedRedstoneRepeaterBlockEntity::new, 
                ModBlocks.ADVANCED_REDSTONE_REPEATER.get(), ModBlocks.ADVANCED_REDSTONE_REPEATER_POWERED.get()).build(null));

    public static final Supplier<BlockEntityType<AnalogEmitterBlockEntity>> ANALOG_EMITTER = 
        BLOCK_ENTITY_TYPES.register("analog_emitter", 
            () -> BlockEntityType.Builder.of(AnalogEmitterBlockEntity::new, 
                ModBlocks.ANALOG_EMITTER.get()).build(null));

    public static final Supplier<BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER =
        BLOCK_ENTITY_TYPES.register("block_breaker",
            () -> BlockEntityType.Builder.of(BlockBreakerBlockEntity::new,
                ModBlocks.BLOCK_BREAKER.get()).build(null));

    public static final Supplier<BlockEntityType<SoundDampenerBlockEntity>> SOUND_DAMPENER =
        BLOCK_ENTITY_TYPES.register("sound_dampener",
            () -> BlockEntityType.Builder.of(SoundDampenerBlockEntity::new,
                ModBlocks.SOUND_DAMPENER.get()).build(null));

    public static final Supplier<BlockEntityType<BlockDestabilizerBlockEntity>> BLOCK_DESTABILIZER = 
        BLOCK_ENTITY_TYPES.register("block_destabilizer", 
            () -> BlockEntityType.Builder.of(BlockDestabilizerBlockEntity::new, 
                ModBlocks.BLOCK_DESTABILIZER.get()).build(null));

    public static final Supplier<BlockEntityType<IronDropperBlockEntity>> IRON_DROPPER = 
        BLOCK_ENTITY_TYPES.register("iron_dropper", 
            () -> BlockEntityType.Builder.of(IronDropperBlockEntity::new, 
                ModBlocks.IRON_DROPPER.get()).build(null));

    public static final Supplier<BlockEntityType<ChatDetectorBlockEntity>> CHAT_DETECTOR = 
        BLOCK_ENTITY_TYPES.register("chat_detector", 
            () -> BlockEntityType.Builder.of(ChatDetectorBlockEntity::new, 
                ModBlocks.CHAT_DETECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<GlobalChatDetectorBlockEntity>> GLOBAL_CHAT_DETECTOR = 
        BLOCK_ENTITY_TYPES.register("global_chat_detector", 
            () -> BlockEntityType.Builder.of(GlobalChatDetectorBlockEntity::new, 
                ModBlocks.GLOBAL_CHAT_DETECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<PeaceCandleBlockEntity>> PEACE_CANDLE = 
        BLOCK_ENTITY_TYPES.register("peace_candle", 
            () -> BlockEntityType.Builder.of(PeaceCandleBlockEntity::new, 
                ModBlocks.PEACE_CANDLE.get()).build(null));

    public static final Supplier<BlockEntityType<PotionVaporizerBlockEntity>> POTION_VAPORIZER = 
        BLOCK_ENTITY_TYPES.register("potion_vaporizer", 
            () -> BlockEntityType.Builder.of(PotionVaporizerBlockEntity::new, 
                ModBlocks.POTION_VAPORIZER.get()).build(null));

    // public static final Supplier<BlockEntityType<OnlineDetectorBlockEntity>> ONLINE_DETECTOR = 
    //     BLOCK_ENTITY_TYPES.register("online_detector", 
    //         () -> BlockEntityType.Builder.of(OnlineDetectorBlockEntity::new, 
    //             ModBlocks.ONLINE_DETECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<DiaphanousBlockEntity>> DIAPHANOUS_BLOCK = 
        BLOCK_ENTITY_TYPES.register("diaphanous_block", 
            () -> BlockEntityType.Builder.of(DiaphanousBlockEntity::new, 
                ModBlocks.DIAPHANOUS_BLOCK.get()).build(null));

    // Ender Bridge System
    public static final Supplier<BlockEntityType<EnderAnchorBlockEntity>> ENDER_ANCHOR = 
        BLOCK_ENTITY_TYPES.register("ender_anchor", 
            () -> BlockEntityType.Builder.of(EnderAnchorBlockEntity::new, 
                ModBlocks.ENDER_ANCHOR.get()).build(null));

    public static final Supplier<BlockEntityType<EnderBridgeBlockEntity>> ENDER_BRIDGE = 
        BLOCK_ENTITY_TYPES.register("ender_bridge", 
            () -> BlockEntityType.Builder.of(EnderBridgeBlockEntity::new, 
                ModBlocks.ENDER_BRIDGE.get()).build(null));

    public static final Supplier<BlockEntityType<PrismarineEnderBridgeBlockEntity>> PRISMARINE_ENDER_BRIDGE =
        BLOCK_ENTITY_TYPES.register("prismarine_ender_bridge",
            () -> BlockEntityType.Builder.of(PrismarineEnderBridgeBlockEntity::new,
                ModBlocks.PRISMARINE_ENDER_BRIDGE.get()).build(null));

    public static final Supplier<BlockEntityType<EnderMailboxBlockEntity>> ENDER_MAILBOX =
        BLOCK_ENTITY_TYPES.register("ender_mailbox",
            () -> BlockEntityType.Builder.of(EnderMailboxBlockEntity::new,
                ModBlocks.ENDER_MAILBOX.get()).build(null));
                
    public static final Supplier<BlockEntityType<FluidDisplayBlockEntity>> FLUID_DISPLAY = 
        BLOCK_ENTITY_TYPES.register("fluid_display", 
            () -> BlockEntityType.Builder.of(FluidDisplayBlockEntity::new, 
                ModBlocks.FLUID_DISPLAY.get()).build(null));

    public static final Supplier<BlockEntityType<IgniterBlockEntity>> IGNITER = 
        BLOCK_ENTITY_TYPES.register("igniter", 
            () -> BlockEntityType.Builder.of(IgniterBlockEntity::new, 
                ModBlocks.IGNITER.get()).build(null));

    public static final Supplier<BlockEntityType<InventoryTesterBlockEntity>> INVENTORY_TESTER = 
        BLOCK_ENTITY_TYPES.register("inventory_tester", 
            () -> BlockEntityType.Builder.of(InventoryTesterBlockEntity::new, 
                ModBlocks.INVENTORY_TESTER.get()).build(null));

    public static final Supplier<BlockEntityType<LightRedirectorBlockEntity>> LIGHT_REDIRECTOR = 
        BLOCK_ENTITY_TYPES.register("light_redirector", 
            () -> BlockEntityType.Builder.of(LightRedirectorBlockEntity::new, 
                ModBlocks.LIGHT_REDIRECTOR.get()).build(null));

    public static final Supplier<BlockEntityType<NotificationInterfaceBlockEntity>> NOTIFICATION_INTERFACE = 
        BLOCK_ENTITY_TYPES.register("notification_interface", 
            () -> BlockEntityType.Builder.of(NotificationInterfaceBlockEntity::new, 
                ModBlocks.NOTIFICATION_INTERFACE.get()).build(null));

    public static final Supplier<BlockEntityType<PlayerInterfaceBlockEntity>> PLAYER_INTERFACE = 
        BLOCK_ENTITY_TYPES.register("player_interface", 
            () -> BlockEntityType.Builder.of(PlayerInterfaceBlockEntity::new, 
                ModBlocks.PLAYER_INTERFACE.get()).build(null));

    // Bean System Block Entities
    public static final Supplier<BlockEntityType<NatureCoreBlockEntity>> NATURE_CORE = 
        BLOCK_ENTITY_TYPES.register("nature_core", 
            () -> BlockEntityType.Builder.of(NatureCoreBlockEntity::new, 
                ModBlocks.NATURE_CORE.get()).build(null));

    public static final Supplier<BlockEntityType<PlantChestBlockEntity>> PLANT_CHEST = 
        BLOCK_ENTITY_TYPES.register("plant_chest", 
            () -> BlockEntityType.Builder.of(PlantChestBlockEntity::new, 
                ModBlocks.PLANT_CHEST.get()).build(null));

}