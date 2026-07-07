package lumien.randomthings.blockentity;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Block-entity type registry. Add types as features are ported.
 */
public final class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<OnlineDetectorBlockEntity>> ONLINE_DETECTOR =
        BLOCK_ENTITY_TYPES.register("online_detector",
            () -> BlockEntityType.Builder.of(OnlineDetectorBlockEntity::new, ModBlocks.ONLINE_DETECTOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<RedstoneObserverBlockEntity>> REDSTONE_OBSERVER =
        BLOCK_ENTITY_TYPES.register("redstone_observer",
            () -> BlockEntityType.Builder.of(RedstoneObserverBlockEntity::new, ModBlocks.REDSTONE_OBSERVER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BasicRedstoneInterfaceBlockEntity>> BASIC_REDSTONE_INTERFACE =
        BLOCK_ENTITY_TYPES.register("basic_redstone_interface",
            () -> BlockEntityType.Builder.of(BasicRedstoneInterfaceBlockEntity::new, ModBlocks.BASIC_REDSTONE_INTERFACE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<EntityDetectorBlockEntity>> ENTITY_DETECTOR =
        BLOCK_ENTITY_TYPES.register("entity_detector",
            () -> BlockEntityType.Builder.of(EntityDetectorBlockEntity::new, ModBlocks.ENTITY_DETECTOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<ChatDetectorBlockEntity>> CHAT_DETECTOR =
        BLOCK_ENTITY_TYPES.register("chat_detector",
            () -> BlockEntityType.Builder.of(ChatDetectorBlockEntity::new, ModBlocks.CHAT_DETECTOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<GlobalChatDetectorBlockEntity>> GLOBAL_CHAT_DETECTOR =
        BLOCK_ENTITY_TYPES.register("global_chat_detector",
            () -> BlockEntityType.Builder.of(GlobalChatDetectorBlockEntity::new, ModBlocks.GLOBAL_CHAT_DETECTOR.get()).build(null));

    // --- Group 2 ---
    public static final RegistrySupplier<BlockEntityType<ItemCollectorBlockEntity>> ITEM_COLLECTOR =
        BLOCK_ENTITY_TYPES.register("item_collector",
            () -> BlockEntityType.Builder.of(ItemCollectorBlockEntity::new, ModBlocks.ITEM_COLLECTOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<InventoryRerouterBlockEntity>> INVENTORY_REROUTER =
        BLOCK_ENTITY_TYPES.register("inventory_rerouter",
            () -> BlockEntityType.Builder.of(InventoryRerouterBlockEntity::new, ModBlocks.INVENTORY_REROUTER.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<AdvancedItemCollectorBlockEntity>> ADVANCED_ITEM_COLLECTOR =
        BLOCK_ENTITY_TYPES.register("advanced_item_collector",
            () -> BlockEntityType.Builder.of(AdvancedItemCollectorBlockEntity::new, ModBlocks.ADVANCED_ITEM_COLLECTOR.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<DyeingMachineBlockEntity>> DYEING_MACHINE =
        BLOCK_ENTITY_TYPES.register("dyeing_machine",
            () -> BlockEntityType.Builder.of(DyeingMachineBlockEntity::new, ModBlocks.DYEING_MACHINE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<ImbuingStationBlockEntity>> IMBUING_STATION =
        BLOCK_ENTITY_TYPES.register("imbuing_station",
            () -> BlockEntityType.Builder.of(ImbuingStationBlockEntity::new, ModBlocks.IMBUING_STATION.get()).build(null));

    // --- Group 5 ---
    public static final RegistrySupplier<BlockEntityType<SoundBoxBlockEntity>> SOUND_BOX =
        BLOCK_ENTITY_TYPES.register("sound_box",
            () -> BlockEntityType.Builder.of(SoundBoxBlockEntity::new, ModBlocks.SOUND_BOX.get()).build(null));

    // --- Group 6 (Floo) ---
    public static final RegistrySupplier<BlockEntityType<FlooBrickBlockEntity>> FLOO_BRICK =
        BLOCK_ENTITY_TYPES.register("floo_brick",
            () -> BlockEntityType.Builder.of(FlooBrickBlockEntity::new, ModBlocks.FLOO_BRICK.get()).build(null));

    // --- Redstone (block-entity) ---
    public static final RegistrySupplier<BlockEntityType<AnalogEmitterBlockEntity>> ANALOG_EMITTER =
        BLOCK_ENTITY_TYPES.register("analog_emitter",
            () -> BlockEntityType.Builder.of(AnalogEmitterBlockEntity::new, ModBlocks.ANALOG_EMITTER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<AdvancedRedstoneTorchBlockEntity>> ADVANCED_REDSTONE_TORCH =
        BLOCK_ENTITY_TYPES.register("advanced_redstone_torch",
            () -> BlockEntityType.Builder.of(AdvancedRedstoneTorchBlockEntity::new,
                ModBlocks.ADVANCED_REDSTONE_TORCH.get(), ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<AdvancedRedstoneRepeaterBlockEntity>> ADVANCED_REDSTONE_REPEATER =
        BLOCK_ENTITY_TYPES.register("advanced_redstone_repeater",
            () -> BlockEntityType.Builder.of(AdvancedRedstoneRepeaterBlockEntity::new,
                ModBlocks.ADVANCED_REDSTONE_REPEATER.get(), ModBlocks.ADVANCED_REDSTONE_REPEATER_POWERED.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<IgniterBlockEntity>> IGNITER =
        BLOCK_ENTITY_TYPES.register("igniter",
            () -> BlockEntityType.Builder.of(IgniterBlockEntity::new, ModBlocks.IGNITER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BlockBreakerBlockEntity>> BLOCK_BREAKER =
        BLOCK_ENTITY_TYPES.register("block_breaker",
            () -> BlockEntityType.Builder.of(BlockBreakerBlockEntity::new, ModBlocks.BLOCK_BREAKER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<IronDropperBlockEntity>> IRON_DROPPER =
        BLOCK_ENTITY_TYPES.register("iron_dropper",
            () -> BlockEntityType.Builder.of(IronDropperBlockEntity::new, ModBlocks.IRON_DROPPER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BloodRoseBlockEntity>> BLOOD_ROSE =
        BLOCK_ENTITY_TYPES.register("blood_rose",
            () -> BlockEntityType.Builder.of(BloodRoseBlockEntity::new, ModBlocks.BLOOD_ROSE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<PeaceCandleBlockEntity>> PEACE_CANDLE =
        BLOCK_ENTITY_TYPES.register("peace_candle",
            () -> BlockEntityType.Builder.of(PeaceCandleBlockEntity::new, ModBlocks.PEACE_CANDLE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<PlayerInterfaceBlockEntity>> PLAYER_INTERFACE =
        BLOCK_ENTITY_TYPES.register("player_interface",
            () -> BlockEntityType.Builder.of(PlayerInterfaceBlockEntity::new, ModBlocks.PLAYER_INTERFACE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<PlantChestBlockEntity>> PLANT_CHEST =
        BLOCK_ENTITY_TYPES.register("plant_chest",
            () -> BlockEntityType.Builder.of(PlantChestBlockEntity::new, ModBlocks.PLANT_CHEST.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<NatureCoreBlockEntity>> NATURE_CORE =
        BLOCK_ENTITY_TYPES.register("nature_core",
            () -> BlockEntityType.Builder.of(NatureCoreBlockEntity::new, ModBlocks.NATURE_CORE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<AncientFurnaceBlockEntity>> ANCIENT_FURNACE =
        BLOCK_ENTITY_TYPES.register("ancient_furnace",
            () -> BlockEntityType.Builder.of(AncientFurnaceBlockEntity::new, ModBlocks.ANCIENT_FURNACE.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<RainShieldBlockEntity>> RAIN_SHIELD =
        BLOCK_ENTITY_TYPES.register("rain_shield",
            () -> BlockEntityType.Builder.of(RainShieldBlockEntity::new, ModBlocks.RAIN_SHIELD.get()).build(null));

    public static final RegistrySupplier<BlockEntityType<BiomeRadarBlockEntity>> BIOME_RADAR =
        BLOCK_ENTITY_TYPES.register("biome_radar",
            () -> BlockEntityType.Builder.of(BiomeRadarBlockEntity::new, ModBlocks.BIOME_RADAR.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<SpectreCoilBlockEntity>> SPECTRE_COIL =
        BLOCK_ENTITY_TYPES.register("spectre_coil",
            () -> BlockEntityType.Builder.of(SpectreCoilBlockEntity::new,
                ModBlocks.SPECTRE_COIL_NORMAL.get(), ModBlocks.SPECTRE_COIL_REDSTONE.get(),
                ModBlocks.SPECTRE_COIL_ENDER.get(), ModBlocks.SPECTRE_COIL_NUMBER.get(),
                ModBlocks.SPECTRE_COIL_GENESIS.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<SpectreEnergyInjectorBlockEntity>> SPECTRE_ENERGY_INJECTOR =
        BLOCK_ENTITY_TYPES.register("spectre_energy_injector",
            () -> BlockEntityType.Builder.of(SpectreEnergyInjectorBlockEntity::new, ModBlocks.SPECTRE_ENERGY_INJECTOR.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<DiaphanousBlockEntity>> DIAPHANOUS_BLOCK =
        BLOCK_ENTITY_TYPES.register("diaphanous_block",
            () -> BlockEntityType.Builder.of(DiaphanousBlockEntity::new, ModBlocks.DIAPHANOUS_BLOCK.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<SoundDampenerBlockEntity>> SOUND_DAMPENER =
        BLOCK_ENTITY_TYPES.register("sound_dampener",
            () -> BlockEntityType.Builder.of(SoundDampenerBlockEntity::new, ModBlocks.SOUND_DAMPENER.get()).build(null));

    private ModBlockEntityTypes() {
    }

    public static void register() {
        BLOCK_ENTITY_TYPES.register();
    }
}
