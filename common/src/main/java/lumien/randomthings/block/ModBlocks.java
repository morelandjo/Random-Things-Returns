package lumien.randomthings.block;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Block registry. Blocks are added here as features are ported.
 *
 * <p>Use {@link #BLOCKS} with Architectury's cross-loader {@link DeferredRegister}. Each entry is a
 * {@link RegistrySupplier} (call {@code .get()} to resolve once registration has run). Remember to
 * register a matching {@code BlockItem} in {@code ModItems} for any block that should be obtainable.</p>
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ModConstants.MOD_ID, Registries.BLOCK);

    // --- Group 1: redstone-themed blocks ---
    public static final RegistrySupplier<OnlineDetectorBlock> ONLINE_DETECTOR =
        BLOCKS.register("online_detector", OnlineDetectorBlock::new);

    public static final RegistrySupplier<RedstoneObserverBlock> REDSTONE_OBSERVER =
        BLOCKS.register("redstone_observer", RedstoneObserverBlock::new);

    public static final RegistrySupplier<BasicRedstoneInterfaceBlock> BASIC_REDSTONE_INTERFACE =
        BLOCKS.register("basic_redstone_interface", BasicRedstoneInterfaceBlock::new);

    public static final RegistrySupplier<EntityDetectorBlock> ENTITY_DETECTOR =
        BLOCKS.register("entity_detector", EntityDetectorBlock::new);

    public static final RegistrySupplier<ChatDetectorBlock> CHAT_DETECTOR =
        BLOCKS.register("chat_detector", ChatDetectorBlock::new);

    public static final RegistrySupplier<GlobalChatDetectorBlock> GLOBAL_CHAT_DETECTOR =
        BLOCKS.register("global_chat_detector", GlobalChatDetectorBlock::new);

    // --- Group 2: item-handling utility blocks ---
    public static final RegistrySupplier<ItemCollectorBlock> ITEM_COLLECTOR =
        BLOCKS.register("item_collector", ItemCollectorBlock::new);

    public static final RegistrySupplier<InventoryRerouterBlock> INVENTORY_REROUTER =
        BLOCKS.register("inventory_rerouter", InventoryRerouterBlock::new);

    public static final RegistrySupplier<AdvancedItemCollectorBlock> ADVANCED_ITEM_COLLECTOR =
        BLOCKS.register("advanced_item_collector", AdvancedItemCollectorBlock::new);

    public static final RegistrySupplier<DyeingMachineBlock> DYEING_MACHINE =
        BLOCKS.register("dyeing_machine", DyeingMachineBlock::new);

    // --- Group 3: crafting stations ---
    public static final RegistrySupplier<ImbuingStationBlock> IMBUING_STATION =
        BLOCKS.register("imbuing_station", ImbuingStationBlock::new);

    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_OAK =
        BLOCKS.register("custom_crafting_table_oak", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_SPRUCE =
        BLOCKS.register("custom_crafting_table_spruce", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_BIRCH =
        BLOCKS.register("custom_crafting_table_birch", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_JUNGLE =
        BLOCKS.register("custom_crafting_table_jungle", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_ACACIA =
        BLOCKS.register("custom_crafting_table_acacia", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_DARK_OAK =
        BLOCKS.register("custom_crafting_table_dark_oak", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_MANGROVE =
        BLOCKS.register("custom_crafting_table_mangrove", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_CHERRY =
        BLOCKS.register("custom_crafting_table_cherry", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_BAMBOO =
        BLOCKS.register("custom_crafting_table_bamboo", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_CRIMSON =
        BLOCKS.register("custom_crafting_table_crimson", CustomCraftingTableBlock::new);
    public static final RegistrySupplier<CustomCraftingTableBlock> CUSTOM_CRAFTING_TABLE_WARPED =
        BLOCKS.register("custom_crafting_table_warped", CustomCraftingTableBlock::new);

    // --- Group 5: decorative / sound ---
    public static final RegistrySupplier<SoundBoxBlock> SOUND_BOX =
        BLOCKS.register("sound_box", () -> new SoundBoxBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(0.8f)
            .sound(SoundType.WOOD)));

    public static final RegistrySupplier<ColoredGrassBlock> COLORED_GRASS =
        BLOCKS.register("colored_grass", () -> new ColoredGrassBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.GRASS)
            .strength(0.6f)
            .sound(SoundType.GRASS)
            .randomTicks()));

    // --- Group 6: player mechanics (Floo) ---
    public static final RegistrySupplier<FlooBrickBlock> FLOO_BRICK =
        BLOCKS.register("floo_brick", FlooBrickBlock::new);

    // --- Group 4: spectre (self-contained material/block subset; dimension + energy network deferred) ---
    public static final RegistrySupplier<SpectreBlockBlock> SPECTRE_BLOCK =
        BLOCKS.register("spectre_block", SpectreBlockBlock::new);

    // --- Decorative / colored blocks ---
    public static final RegistrySupplier<StainedBrickBlock> STAINED_BRICK =
        BLOCKS.register("stained_brick", StainedBrickBlock::new);
    public static final RegistrySupplier<LuminousStainedBrickBlock> LUMINOUS_STAINED_BRICK =
        BLOCKS.register("luminous_stained_brick", LuminousStainedBrickBlock::new);
    public static final RegistrySupplier<RainbowLampBlock> RAINBOW_LAMP =
        BLOCKS.register("rainbow_lamp", RainbowLampBlock::new);
    public static final RegistrySupplier<SuperLubricentStoneBlock> SUPER_LUBRICENT_STONE =
        BLOCKS.register("super_lubricent_stone", SuperLubricentStoneBlock::new);
    public static final RegistrySupplier<LapisGlassBlock> LAPIS_GLASS =
        BLOCKS.register("lapis_glass", LapisGlassBlock::new);
    public static final RegistrySupplier<QuartzGlassBlock> QUARTZ_GLASS =
        BLOCKS.register("quartz_glass", QuartzGlassBlock::new);
    public static final RegistrySupplier<BiomeGlassBlock> BIOME_GLASS =
        BLOCKS.register("biome_glass", BiomeGlassBlock::new);
    public static final RegistrySupplier<BiomeStoneBlock> BIOME_STONE =
        BLOCKS.register("biome_stone", BiomeStoneBlock::new);

    // --- Redstone (no-block-entity) ---
    public static final RegistrySupplier<SidedRedstoneBlock> SIDED_REDSTONE =
        BLOCKS.register("sided_redstone", () -> new SidedRedstoneBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.FIRE)
            .requiresCorrectToolForDrops()
            .strength(0.0F)
            .instabreak()));
    public static final RegistrySupplier<ContactButtonBlock> CONTACT_BUTTON =
        BLOCKS.register("contact_button", ContactButtonBlock::new);
    public static final RegistrySupplier<ContactLeverBlock> CONTACT_LEVER =
        BLOCKS.register("contact_lever", ContactLeverBlock::new);
    public static final RegistrySupplier<AnalogEmitterBlock> ANALOG_EMITTER =
        BLOCKS.register("analog_emitter", AnalogEmitterBlock::new);
    public static final RegistrySupplier<AdvancedRedstoneTorchBlock> ADVANCED_REDSTONE_TORCH =
        BLOCKS.register("advanced_redstone_torch", AdvancedRedstoneTorchBlock::new);
    public static final RegistrySupplier<RainShieldBlock> RAIN_SHIELD =
        BLOCKS.register("rain_shield", RainShieldBlock::new);
    public static final RegistrySupplier<AdvancedRedstoneWallTorchBlock> ADVANCED_WALL_REDSTONE_TORCH =
        BLOCKS.register("advanced_redstone_wall_torch", AdvancedRedstoneWallTorchBlock::new);
    public static final RegistrySupplier<AdvancedRedstoneRepeaterBlock> ADVANCED_REDSTONE_REPEATER =
        BLOCKS.register("advanced_redstone_repeater", () -> new AdvancedRedstoneRepeaterBlock(false));
    public static final RegistrySupplier<AdvancedRedstoneRepeaterBlock> ADVANCED_REDSTONE_REPEATER_POWERED =
        BLOCKS.register("advanced_redstone_repeater_powered", () -> new AdvancedRedstoneRepeaterBlock(true));
    public static final RegistrySupplier<IgniterBlock> IGNITER =
        BLOCKS.register("igniter", () -> new IgniterBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE)));
    public static final RegistrySupplier<BlockBreakerBlock> BLOCK_BREAKER =
        BLOCKS.register("block_breaker", () -> new BlockBreakerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(1.5F, 6.0F).sound(SoundType.STONE)));
    public static final RegistrySupplier<IronDropperBlock> IRON_DROPPER =
        BLOCKS.register("iron_dropper", IronDropperBlock::new);

    // --- Plants ---
    public static final RegistrySupplier<BloodRoseBlock> BLOOD_ROSE =
        BLOCKS.register("blood_rose", BloodRoseBlock::new);
    public static final RegistrySupplier<GlowingMushroomBlock> GLOWING_MUSHROOM =
        BLOCKS.register("glowing_mushroom", GlowingMushroomBlock::new);
    public static final RegistrySupplier<LotusBlock> LOTUS =
        BLOCKS.register("lotus", LotusBlock::new);

    // --- Bean system ---
    public static final RegistrySupplier<BlockBeanSprout> BEANSPROUT =
        BLOCKS.register("beansprout", BlockBeanSprout::new);
    public static final RegistrySupplier<BlockBeanStalk> BEANSTALK =
        BLOCKS.register("beanstalk", BlockBeanStalk::new);
    public static final RegistrySupplier<BlockSpecialBeanStalk> SPECIALBEANSTALK =
        BLOCKS.register("specialbeanstalk", BlockSpecialBeanStalk::new);
    public static final RegistrySupplier<BlockBeanPod> BEANPOD =
        BLOCKS.register("beanpod", BlockBeanPod::new);
    public static final RegistrySupplier<PitcherPlantBlock> PITCHER_PLANT =
        BLOCKS.register("pitcher_plant", PitcherPlantBlock::new);

    // --- Misc decorative ---
    public static final RegistrySupplier<CompressedSlimeBlock> COMPRESSED_SLIME_BLOCK =
        BLOCKS.register("compressed_slime_block", CompressedSlimeBlock::new);
    public static final RegistrySupplier<TriggerGlassBlock> TRIGGER_GLASS =
        BLOCKS.register("trigger_glass", TriggerGlassBlock::new);
    public static final RegistrySupplier<LapisLampBlock> LAPIS_LAMP =
        BLOCKS.register("lapis_lamp", LapisLampBlock::new);
    public static final RegistrySupplier<QuartzLampBlock> QUARTZ_LAMP =
        BLOCKS.register("quartz_lamp", QuartzLampBlock::new);
    public static final RegistrySupplier<LuminousBlock> LUMINOUS_BLOCK =
        BLOCKS.register("luminous_block", LuminousBlock::new);
    public static final RegistrySupplier<TranslucentLuminousBlock> TRANSLUCENT_LUMINOUS_BLOCK =
        BLOCKS.register("translucent_luminous_block", TranslucentLuminousBlock::new);
    public static final RegistrySupplier<AncientBrickBlock> ANCIENT_BRICK =
        BLOCKS.register("ancient_brick", AncientBrickBlock::new);
    public static final RegistrySupplier<AncientFurnaceBlock> ANCIENT_FURNACE =
        BLOCKS.register("ancient_furnace", AncientFurnaceBlock::new);
    public static final RegistrySupplier<BlockNatureCore> NATURE_CORE =
        BLOCKS.register("nature_core", BlockNatureCore::new);
    public static final RegistrySupplier<PlantChestBlock> PLANT_CHEST =
        BLOCKS.register("plant_chest", PlantChestBlock::new);
    public static final RegistrySupplier<SpectreCoreBlock> SPECTRE_CORE =
        BLOCKS.register("spectre_core", SpectreCoreBlock::new);
    public static final RegistrySupplier<FertilizedDirtBlock> FERTILIZED_DIRT =
        BLOCKS.register("fertilized_dirt", FertilizedDirtBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_OAK = BLOCKS.register("platform_oak", PlatformBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_SPRUCE = BLOCKS.register("platform_spruce", PlatformBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_BIRCH = BLOCKS.register("platform_birch", PlatformBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_JUNGLE = BLOCKS.register("platform_jungle", PlatformBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_ACACIA = BLOCKS.register("platform_acacia", PlatformBlock::new);
    public static final RegistrySupplier<PlatformBlock> PLATFORM_DARKOAK = BLOCKS.register("platform_darkoak", PlatformBlock::new);
    public static final RegistrySupplier<SticksBlock> BLOCK_OF_STICKS =
        BLOCKS.register("block_of_sticks", () -> new SticksBlock(false));
    public static final RegistrySupplier<SticksBlock> BLOCK_OF_STICKS_RETURNING =
        BLOCKS.register("block_of_sticks_returning", () -> new SticksBlock(true));
    public static final RegistrySupplier<PeaceCandleBlock> PEACE_CANDLE =
        BLOCKS.register("peace_candle", PeaceCandleBlock::new);
    public static final RegistrySupplier<BlazingFireBlock> BLAZING_FIRE =
        BLOCKS.register("blazing_fire", () -> new BlazingFireBlock(
            BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.FIRE)));
    public static final RegistrySupplier<PlayerInterfaceBlock> PLAYER_INTERFACE =
        BLOCKS.register("player_interface", PlayerInterfaceBlock::new);
    public static final RegistrySupplier<BiomeRadarBlock> BIOME_RADAR =
        BLOCKS.register("biome_radar", BiomeRadarBlock::new);

    // --- Spectre energy network ---
    public static final RegistrySupplier<SpectreCoilBlock> SPECTRE_COIL_NORMAL =
        BLOCKS.register("spectre_coil_normal", () -> new SpectreCoilBlock(SpectreCoilBlock.CoilType.NORMAL));
    public static final RegistrySupplier<SpectreCoilBlock> SPECTRE_COIL_REDSTONE =
        BLOCKS.register("spectre_coil_redstone", () -> new SpectreCoilBlock(SpectreCoilBlock.CoilType.REDSTONE));
    public static final RegistrySupplier<SpectreCoilBlock> SPECTRE_COIL_ENDER =
        BLOCKS.register("spectre_coil_ender", () -> new SpectreCoilBlock(SpectreCoilBlock.CoilType.ENDER));
    public static final RegistrySupplier<SpectreCoilBlock> SPECTRE_COIL_NUMBER =
        BLOCKS.register("spectre_coil_number", () -> new SpectreCoilBlock(SpectreCoilBlock.CoilType.NUMBER));
    public static final RegistrySupplier<SpectreCoilBlock> SPECTRE_COIL_GENESIS =
        BLOCKS.register("spectre_coil_genesis", () -> new SpectreCoilBlock(SpectreCoilBlock.CoilType.GENESIS));
    public static final RegistrySupplier<SpectreEnergyInjectorBlock> SPECTRE_ENERGY_INJECTOR =
        BLOCKS.register("spectre_energy_injector", SpectreEnergyInjectorBlock::new);

    public static final RegistrySupplier<DiaphanousBlock> DIAPHANOUS_BLOCK =
        BLOCKS.register("diaphanous_block", () -> new DiaphanousBlock(BlockBehaviour.Properties.of()));

    // --- Sound subsystem ---
    public static final RegistrySupplier<SoundDampenerBlock> SOUND_DAMPENER =
        BLOCKS.register("sound_dampener", SoundDampenerBlock::new);

    private ModBlocks() {
    }

    public static void register() {
        BLOCKS.register();
    }
}
