package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.BiomeStoneBlock;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.awt.Color;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModConstants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModConstants.MOD_ID);

    // Block Items
    public static final Supplier<Item> FERTILIZED_DIRT = ITEMS.register("fertilized_dirt",
        () -> new BlockItem(ModBlocks.FERTILIZED_DIRT.get(), new Item.Properties()));

    public static final Supplier<Item> RAINBOW_LAMP = ITEMS.register("rainbow_lamp",
        () -> new BlockItem(ModBlocks.RAINBOW_LAMP.get(), new Item.Properties()));

    public static final Supplier<Item> RAIN_SHIELD = ITEMS.register("rain_shield",
        () -> new BlockItem(ModBlocks.RAIN_SHIELD.get(), new Item.Properties()));
        
    public static final Supplier<Item> SLIME_CUBE = ITEMS.register("slime_cube",
        () -> new BlockItem(ModBlocks.SLIME_CUBE.get(), new Item.Properties()));

    public static final Supplier<Item> SUPER_LUBRICENT_STONE = ITEMS.register("super_lubricent_stone",
        () -> new BlockItem(ModBlocks.SUPER_LUBRICENT_STONE.get(), new Item.Properties()));

    public static final Supplier<Item> ADVANCED_REDSTONE_TORCH = ITEMS.register("advanced_redstone_torch",
        () -> new StandingAndWallBlockItem(ModBlocks.ADVANCED_REDSTONE_TORCH.get(), ModBlocks.ADVANCED_WALL_REDSTONE_TORCH.get(), new Item.Properties(), net.minecraft.core.Direction.DOWN));

    public static final Supplier<Item> BLOCK_OF_STICKS = ITEMS.register("block_of_sticks",
        () -> new BlockItem(ModBlocks.BLOCK_OF_STICKS.get(), new Item.Properties()));

    public static final Supplier<Item> BLOCK_OF_STICKS_RETURNING = ITEMS.register("block_of_sticks_returning",
        () -> new BlockItem(ModBlocks.BLOCK_OF_STICKS_RETURNING.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_OAK = ITEMS.register("platform_oak",
        () -> new BlockItem(ModBlocks.PLATFORM_OAK.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_SPRUCE = ITEMS.register("platform_spruce",
        () -> new BlockItem(ModBlocks.PLATFORM_SPRUCE.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_BIRCH = ITEMS.register("platform_birch",
        () -> new BlockItem(ModBlocks.PLATFORM_BIRCH.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_JUNGLE = ITEMS.register("platform_jungle",
        () -> new BlockItem(ModBlocks.PLATFORM_JUNGLE.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_ACACIA = ITEMS.register("platform_acacia",
        () -> new BlockItem(ModBlocks.PLATFORM_ACACIA.get(), new Item.Properties()));

    public static final Supplier<Item> PLATFORM_DARKOAK = ITEMS.register("platform_darkoak",
        () -> new BlockItem(ModBlocks.PLATFORM_DARKOAK.get(), new Item.Properties()));

    public static final Supplier<Item> BLOOD_ROSE = ITEMS.register("blood_rose",
        () -> new BlockItem(ModBlocks.BLOOD_ROSE.get(), new Item.Properties()));

    public static final Supplier<Item> GLOWING_MUSHROOM = ITEMS.register("glowing_mushroom",
        () -> new BlockItem(ModBlocks.GLOWING_MUSHROOM.get(), new Item.Properties()));

    public static final Supplier<Item> ADVANCED_REDSTONE_REPEATER = ITEMS.register("advanced_redstone_repeater",
        () -> new BlockItem(ModBlocks.ADVANCED_REDSTONE_REPEATER.get(), new Item.Properties()));

    public static final Supplier<Item> ANALOG_EMITTER = ITEMS.register("analog_emitter",
        () -> new BlockItem(ModBlocks.ANALOG_EMITTER.get(), new Item.Properties()));

    public static final Supplier<Item> SIDED_REDSTONE = ITEMS.register("sided_redstone",
        () -> new BlockItem(ModBlocks.SIDED_REDSTONE.get(), new Item.Properties()));

    public static final Supplier<Item> BLOCK_BREAKER = ITEMS.register("block_breaker",
        () -> new BlockItem(ModBlocks.BLOCK_BREAKER.get(), new Item.Properties()));

    public static final Supplier<Item> SOUND_DAMPENER = ITEMS.register("sound_dampener",
        () -> new BlockItem(ModBlocks.SOUND_DAMPENER.get(), new Item.Properties()));

    public static final Supplier<Item> BLOCK_DESTABILIZER = ITEMS.register("block_destabilizer",
        () -> new BlockItem(ModBlocks.BLOCK_DESTABILIZER.get(), new Item.Properties()));

    public static final Supplier<Item> IRON_DROPPER = ITEMS.register("iron_dropper",
        () -> new BlockItem(ModBlocks.IRON_DROPPER.get(), new Item.Properties()));

    public static final Supplier<Item> CHAT_DETECTOR = ITEMS.register("chat_detector",
        () -> new BlockItem(ModBlocks.CHAT_DETECTOR.get(), new Item.Properties()));

    public static final Supplier<Item> GLOBAL_CHAT_DETECTOR = ITEMS.register("global_chat_detector",
        () -> new BlockItem(ModBlocks.GLOBAL_CHAT_DETECTOR.get(), new Item.Properties()));

    public static final Supplier<Item> PEACE_CANDLE = ITEMS.register("peace_candle",
        () -> new BlockItem(ModBlocks.PEACE_CANDLE.get(), new Item.Properties()));

    public static final Supplier<Item> POTION_VAPORIZER = ITEMS.register("potion_vaporizer",
        () -> new BlockItem(ModBlocks.POTION_VAPORIZER.get(), new Item.Properties()));

    public static final Supplier<Item> COMPRESSED_SLIME_BLOCK = ITEMS.register("compressed_slime_block",
        () -> new BlockItem(ModBlocks.COMPRESSED_SLIME_BLOCK.get(), new Item.Properties()));

    public static final Supplier<Item> CONTACT_BUTTON = ITEMS.register("contact_button",
        () -> new BlockItem(ModBlocks.CONTACT_BUTTON.get(), new Item.Properties()));

    public static final Supplier<Item> CONTACT_LEVER = ITEMS.register("contact_lever",
        () -> new BlockItem(ModBlocks.CONTACT_LEVER.get(), new Item.Properties()));

    // Biome Block Items
    public static final Supplier<Item> BIOME_STONE_SMOOTH = ITEMS.register("biome_stone_smooth",
        () -> new BiomeStoneBlockItem(ModBlocks.BIOME_STONE.get(), new Item.Properties(), BiomeStoneBlock.Variant.SMOOTH));
    
    public static final Supplier<Item> BIOME_STONE_COBBLE = ITEMS.register("biome_stone_cobble",
        () -> new BiomeStoneBlockItem(ModBlocks.BIOME_STONE.get(), new Item.Properties(), BiomeStoneBlock.Variant.COBBLE));
    
    public static final Supplier<Item> BIOME_STONE_BRICK = ITEMS.register("biome_stone_brick",
        () -> new BiomeStoneBlockItem(ModBlocks.BIOME_STONE.get(), new Item.Properties(), BiomeStoneBlock.Variant.BRICK));
    
    public static final Supplier<Item> BIOME_STONE_CRACKED = ITEMS.register("biome_stone_cracked",
        () -> new BiomeStoneBlockItem(ModBlocks.BIOME_STONE.get(), new Item.Properties(), BiomeStoneBlock.Variant.CRACKED));
    
    public static final Supplier<Item> BIOME_STONE_CHISELED = ITEMS.register("biome_stone_chiseled",
        () -> new BiomeStoneBlockItem(ModBlocks.BIOME_STONE.get(), new Item.Properties(), BiomeStoneBlock.Variant.CHISELED));
    
    public static final Supplier<Item> BIOME_GLASS = ITEMS.register("biome_glass",
        () -> new BlockItem(ModBlocks.BIOME_GLASS.get(), new Item.Properties()));

    public static final Supplier<Item> LAPIS_GLASS = ITEMS.register("lapis_glass",
        () -> new BlockItem(ModBlocks.LAPIS_GLASS.get(), new Item.Properties()));

    public static final Supplier<Item> LAPIS_LAMP = ITEMS.register("lapis_lamp",
        () -> new BlockItem(ModBlocks.LAPIS_LAMP.get(), new Item.Properties()));
    
    public static final Supplier<Item> DIAPHANOUS_BLOCK = ITEMS.register("diaphanous_block",
        () -> new DiaphanousBlockItem(ModBlocks.DIAPHANOUS_BLOCK.get(), new Item.Properties()));

    // Bean System Block Items
    public static final Supplier<Item> BEANSPROUT = ITEMS.register("beansprout",
        () -> new BlockItem(ModBlocks.BEANSPROUT.get(), new Item.Properties()));

    public static final Supplier<Item> BEANSTALK = ITEMS.register("beanstalk",
        () -> new BlockItem(ModBlocks.BEANSTALK.get(), new Item.Properties()));

    public static final Supplier<Item> SPECIALBEANSTALK = ITEMS.register("specialbeanstalk",
        () -> new BlockItem(ModBlocks.SPECIALBEANSTALK.get(), new Item.Properties()));

    public static final Supplier<Item> BEANPOD = ITEMS.register("beanpod",
        () -> new BlockItem(ModBlocks.BEANPOD.get(), new Item.Properties()));

    public static final Supplier<Item> NATURE_CORE = ITEMS.register("nature_core",
        () -> new BlockItem(ModBlocks.NATURE_CORE.get(), new Item.Properties()));

    public static final Supplier<Item> PLANT_CHEST = ITEMS.register("plant_chest",
        () -> new PlantChestItem(ModBlocks.PLANT_CHEST.get(), new Item.Properties()));

    // Regular Items
    public static final Supplier<Item> BIOME_CRYSTAL = ITEMS.register("biome_crystal",
        () -> new BiomeCrystalItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> BLOOD_ROSE_PETAL = ITEMS.register("blood_rose_petal",
        () -> new Item(new Item.Properties()));
    public static final Supplier<Item> POSITION_FILTER = ITEMS.register("position_filter",
        () -> new PositionFilterItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> ITEM_FILTER = ITEMS.register("item_filter",
        () -> new ItemFilterItem(new Item.Properties()));

    public static final Supplier<Item> ENTITY_FILTER = ITEMS.register("entity_filter",
        () -> new EntityFilterItem(new Item.Properties()));

    // Lava Protection Items
    public static final Supplier<Item> LAVA_CHARM = ITEMS.register("lava_charm",
        () -> new LavaCharmItem());

    public static final Supplier<Item> OBSIDIAN_SKULL = ITEMS.register("obsidian_skull",
        () -> new ObsidianSkullItem());

    // Water Walking Boots
    public static final Supplier<Item> WATER_WALKING_BOOTS = ITEMS.register("water_walking_boots",
        () -> new WaterWalkingBootsItem(new Item.Properties()));

    public static final Supplier<Item> OBSIDIAN_WATER_WALKING_BOOTS = ITEMS.register("obsidian_water_walking_boots",
        () -> new ObsidianWaterWalkingBootsItem(new Item.Properties()));

    public static final Supplier<Item> LAVA_WADERS = ITEMS.register("lava_waders",
        () -> new LavaWadersItem(new Item.Properties()));

    // Magic Hood
    public static final Supplier<Item> MAGIC_HOOD = ITEMS.register("magic_hood",
        () -> new MagicHoodItem(new Item.Properties()));

    // Super Lubricent Items
    public static final Supplier<Item> SUPER_LUBRICENT_TINCTURE = ITEMS.register("superlubricenttincture",
        () -> new SuperLubricentTinctureItem());

    public static final Supplier<Item> SUPER_LUBRICENT_BOOTS = ITEMS.register("superlubricentboots",
        () -> new SuperLubricentBootsItem(new Item.Properties()));

    // Summoning Pendulum
    public static final Supplier<Item> SUMMONING_PENDULUM = ITEMS.register("summoning_pendulum",
        () -> new SummoningPendulumItem());

    // Ectoplasm System Items
    public static final Supplier<Item> ECTOPLASM = ITEMS.register("ectoplasm",
        () -> new EctoplasmItem());
    public static final Supplier<Item> SPECTRE_INGOT = ITEMS.register("spectre_ingot",
        () -> new SpectreIngotItem());
    public static final Supplier<Item> SPECTRE_ANCHOR = ITEMS.register("spectre_anchor",
        () -> new SpectreAnchorItem(new Item.Properties()));
    public static final Supplier<Item> SPECTRE_ILLUMINATOR = ITEMS.register("spectre_illuminator",
        () -> new SpectreIlluminatorItem());
    public static final Supplier<Item> BLACKOUT_POWDER = ITEMS.register("blackout_powder",
        () -> new BlackoutPowderItem());

    // Spectre Dimension Items
    public static final Supplier<Item> SPECTRE_KEY = ITEMS.register("spectre_key",
        () -> new SpectreKeyItem(new Item.Properties()));
    public static final Supplier<Item> SPECTRE_BLOCK = ITEMS.register("spectre_block",
        () -> new BlockItem(ModBlocks.SPECTRE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<Item> SPECTRE_CORE = ITEMS.register("spectre_core",
        () -> new BlockItem(ModBlocks.SPECTRE_CORE.get(), new Item.Properties()));

    // Bean Items
    public static final Supplier<Item> BEAN = ITEMS.register("bean",
        () -> new ItemBean());

    public static final Supplier<Item> LESSERBEAN = ITEMS.register("lesserbean",
        () -> new ItemLesserMagicBean());

    public static final Supplier<Item> MAGICBEAN = ITEMS.register("magicbean",
        () -> new ItemMagicBean());

    public static final Supplier<Item> BEANSTEW = ITEMS.register("beanstew",
        () -> new ItemBeanStew());

    public static final Supplier<Item> GOLDEN_EGG = ITEMS.register("golden_egg",
        () -> new GoldenEggItem(new Item.Properties()));

    public static final Supplier<Item> ID_CARD = ITEMS.register("id_card",
        () -> new IdCardItem(new Item.Properties().stacksTo(1)));

    // Divining Rods
    public static final Supplier<Item> DIVINING_ROD_COAL = ITEMS.register("divining_rod_coal",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(20, 20, 20, 50)}, new String[]{"minecraft:coal_ores"}));

    public static final Supplier<Item> DIVINING_ROD_IRON = ITEMS.register("divining_rod_iron",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(211, 180, 159, 50)}, new String[]{"minecraft:iron_ores"}));

    public static final Supplier<Item> DIVINING_ROD_GOLD = ITEMS.register("divining_rod_gold",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(246, 233, 80, 50)}, new String[]{"minecraft:gold_ores"}));

    public static final Supplier<Item> DIVINING_ROD_LAPIS = ITEMS.register("divining_rod_lapis",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(5, 45, 150, 50)}, new String[]{"minecraft:lapis_ores"}));

    public static final Supplier<Item> DIVINING_ROD_REDSTONE = ITEMS.register("divining_rod_redstone",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(211, 1, 1, 50)}, new String[]{"minecraft:redstone_ores"}));

    public static final Supplier<Item> DIVINING_ROD_EMERALD = ITEMS.register("divining_rod_emerald",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(0, 220, 0, 50)}, new String[]{"minecraft:emerald_ores"}));

    public static final Supplier<Item> DIVINING_ROD_DIAMOND = ITEMS.register("divining_rod_diamond",
        () -> new DiviningRodItem(new Item.Properties(), new Color[]{new Color(87, 221, 229, 50)}, new String[]{"minecraft:diamond_ores"}));

    public static final Supplier<Item> DIVINING_ROD_VANILLA = ITEMS.register("divining_rod_vanilla",
        () -> new DiviningRodItem(new Item.Properties(), getAllColors(), getAllTags()));

    // Spectre Tools
    public static final Supplier<Item> SPECTRE_PICKAXE = ITEMS.register("spectre_pickaxe",
        () -> new SpectrePickaxeItem(new Item.Properties()));

    public static final Supplier<Item> SPECTRE_SWORD = ITEMS.register("spectre_sword",
        () -> new SpectreSwordItem(new Item.Properties()));

    public static final Supplier<Item> SPECTRE_SHOVEL = ITEMS.register("spectre_shovel",
        () -> new SpectreShovelItem(new Item.Properties()));

    public static final Supplier<Item> SPECTRE_AXE = ITEMS.register("spectre_axe",
        () -> new SpectreAxeItem(new Item.Properties()));

    // Tools
    public static final Supplier<Item> EMERALD_COMPASS = ITEMS.register("emerald_compass",
        () -> new EmeraldCompassItem(new Item.Properties()));

    public static final Supplier<Item> GOLDEN_COMPASS = ITEMS.register("golden_compass",
        () -> new GoldenCompassItem(new Item.Properties()));

    public static final Supplier<Item> BLAZE_AND_STEEL = ITEMS.register("blaze_and_steel",
        () -> new BlazeAndSteelItem(new Item.Properties()));

    public static final Supplier<Item> REDSTONE_ACTIVATOR = ITEMS.register("redstone_activator",
        () -> new RedstoneActivatorItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> REDSTONE_REMOTE = ITEMS.register("redstone_remote",
        () -> new RedstoneRemoteItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> CHUNK_ANALYZER = ITEMS.register("chunk_analyzer",
        () -> new ChunkAnalyzerItem(new Item.Properties()));

    public static final Supplier<Item> SOUND_PATTERN = ITEMS.register("sound_pattern",
        () -> new ItemSoundPattern());

    public static final Supplier<Item> SOUND_RECORDER = ITEMS.register("sound_recorder",
        () -> new SoundRecorderItem());

    public static final Supplier<Item> PORTABLE_SOUND_DAMPENER = ITEMS.register("portable_sound_dampener",
        () -> new ItemPortableSoundDampener());

    public static final Supplier<Item> ECLIPSED_CLOCK = ITEMS.register("eclipsed_clock",
        () -> new EclipsedClockItem(new Item.Properties()));
        
    public static final Supplier<Item> TIME_IN_A_BOTTLE = ITEMS.register("time_in_a_bottle",
        () -> new TimeInABottleItem(new Item.Properties()));

    public static final Supplier<Item> ESCAPE_ROPE = ITEMS.register("escape_rope",
        () -> new EscapeRopeItem());

    // Ender Bridge System
    public static final Supplier<Item> STABLE_ENDER_PEARL = ITEMS.register("stable_ender_pearl",
        () -> new StableEnderPearlItem());

    public static final Supplier<Item> PORTKEY = ITEMS.register("portkey",
        () -> new PortkeyItem(new Item.Properties().stacksTo(1)));

    // Ender Buckets
    public static final Supplier<Item> ENDER_BUCKET = ITEMS.register("ender_bucket",
        () -> new EnderBucketItem());

    public static final Supplier<Item> REINFORCED_ENDER_BUCKET = ITEMS.register("reinforced_ender_bucket",
        () -> new ReinforcedEnderBucketItem());

    public static final Supplier<Item> ENDER_LETTER = ITEMS.register("ender_letter",
        () -> new EnderLetterItem(new Item.Properties()));

    public static final Supplier<Item> ENDER_ANCHOR = ITEMS.register("ender_anchor",
        () -> new BlockItem(ModBlocks.ENDER_ANCHOR.get(), new Item.Properties()));

    public static final Supplier<Item> ENDER_BRIDGE = ITEMS.register("ender_bridge",
        () -> new BlockItem(ModBlocks.ENDER_BRIDGE.get(), new Item.Properties()));

    public static final Supplier<Item> ENDER_MAILBOX = ITEMS.register("ender_mailbox",
        () -> new BlockItem(ModBlocks.ENDER_MAILBOX.get(), new Item.Properties()));

    public static final Supplier<Item> PRISMARINE_ENDER_BRIDGE = ITEMS.register("prismarine_ender_bridge",
        () -> new BlockItem(ModBlocks.PRISMARINE_ENDER_BRIDGE.get(), new Item.Properties()));
        
    public static final Supplier<Item> FLUID_DISPLAY = ITEMS.register("fluid_display",
        () -> new BlockItem(ModBlocks.FLUID_DISPLAY.get(), new Item.Properties()));

    public static final Supplier<Item> IGNITER = ITEMS.register("igniter",
        () -> new BlockItem(ModBlocks.IGNITER.get(), new Item.Properties()));

    public static final Supplier<Item> INVENTORY_TESTER = ITEMS.register("inventory_tester",
        () -> new BlockItem(ModBlocks.INVENTORY_TESTER.get(), new Item.Properties()));

    public static final Supplier<Item> LIGHT_REDIRECTOR = ITEMS.register("light_redirector",
        () -> new BlockItem(ModBlocks.LIGHT_REDIRECTOR.get(), new Item.Properties()));

    public static final Supplier<Item> NOTIFICATION_INTERFACE = ITEMS.register("notification_interface",
        () -> new BlockItem(ModBlocks.NOTIFICATION_INTERFACE.get(), new Item.Properties()));

    public static final Supplier<Item> PLAYER_INTERFACE = ITEMS.register("player_interface",
        () -> new PlayerInterfaceItem(ModBlocks.PLAYER_INTERFACE.get(), new Item.Properties()));

    public static final Supplier<Item> QUARTZ_GLASS = ITEMS.register("quartz_glass",
        () -> new BlockItem(ModBlocks.QUARTZ_GLASS.get(), new Item.Properties()));

    public static final Supplier<Item> TRIGGER_GLASS = ITEMS.register("trigger_glass",
        () -> new BlockItem(ModBlocks.TRIGGER_GLASS.get(), new Item.Properties()));

    public static final Supplier<Item> QUARTZ_LAMP = ITEMS.register("quartz_lamp",
        () -> new BlockItem(ModBlocks.QUARTZ_LAMP.get(), new Item.Properties()));

    public static final Supplier<Item> PITCHER_PLANT = ITEMS.register("pitcher_plant",
        () -> new BlockItem(ModBlocks.PITCHER_PLANT.get(), new Item.Properties()));

    public static final Supplier<Item> LOTUS_SEEDS = ITEMS.register("lotus_seeds",
        () -> new LotusSeedsItem());

    public static final Supplier<Item> LOTUS_BLOSSOM = ITEMS.register("lotus_blossom",
        () -> new LotusBlossomItem());

    // Luminous Block Items
    public static final Supplier<Item> LUMINOUS_POWDER = ITEMS.register("luminous_powder",
        () -> new LuminousPowderItem());

    // Rune System Items
    public static final Supplier<Item> RUNE_DUST = ITEMS.register("rune_dust",
        () -> new RuneDustItem(new Item.Properties()
            .component(ModDataComponents.RUNE_COLOR.get(), DyeColor.WHITE))); // Default white component

    public static final Supplier<Item> RUNE_PATTERN = ITEMS.register("rune_pattern",
        () -> new RunePatternItem());

    public static final Supplier<Item> RUNE_BASE = ITEMS.register("rune_base",
        () -> new BlockItem(ModBlocks.RUNE_BASE.get(), new Item.Properties()));

    // Regular Luminous Blocks - All 16 colors
    public static final Supplier<Item> LUMINOUS_BLOCK_WHITE = ITEMS.register("luminous_block_white",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.WHITE));
    public static final Supplier<Item> LUMINOUS_BLOCK_ORANGE = ITEMS.register("luminous_block_orange",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.ORANGE));
    public static final Supplier<Item> LUMINOUS_BLOCK_MAGENTA = ITEMS.register("luminous_block_magenta",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.MAGENTA));
    public static final Supplier<Item> LUMINOUS_BLOCK_LIGHT_BLUE = ITEMS.register("luminous_block_light_blue",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIGHT_BLUE));
    public static final Supplier<Item> LUMINOUS_BLOCK_YELLOW = ITEMS.register("luminous_block_yellow",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.YELLOW));
    public static final Supplier<Item> LUMINOUS_BLOCK_LIME = ITEMS.register("luminous_block_lime",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIME));
    public static final Supplier<Item> LUMINOUS_BLOCK_PINK = ITEMS.register("luminous_block_pink",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.PINK));
    public static final Supplier<Item> LUMINOUS_BLOCK_GRAY = ITEMS.register("luminous_block_gray",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.GRAY));
    public static final Supplier<Item> LUMINOUS_BLOCK_LIGHT_GRAY = ITEMS.register("luminous_block_light_gray",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIGHT_GRAY));
    public static final Supplier<Item> LUMINOUS_BLOCK_CYAN = ITEMS.register("luminous_block_cyan",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.CYAN));
    public static final Supplier<Item> LUMINOUS_BLOCK_PURPLE = ITEMS.register("luminous_block_purple",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.PURPLE));
    public static final Supplier<Item> LUMINOUS_BLOCK_BLUE = ITEMS.register("luminous_block_blue",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BLUE));
    public static final Supplier<Item> LUMINOUS_BLOCK_BROWN = ITEMS.register("luminous_block_brown",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BROWN));
    public static final Supplier<Item> LUMINOUS_BLOCK_GREEN = ITEMS.register("luminous_block_green",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.GREEN));
    public static final Supplier<Item> LUMINOUS_BLOCK_RED = ITEMS.register("luminous_block_red",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.RED));
    public static final Supplier<Item> LUMINOUS_BLOCK_BLACK = ITEMS.register("luminous_block_black",
        () -> new LuminousBlockItem(ModBlocks.LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BLACK));

    // Translucent Luminous Blocks - All 16 colors  
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_WHITE = ITEMS.register("translucent_luminous_block_white",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.WHITE));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_ORANGE = ITEMS.register("translucent_luminous_block_orange",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.ORANGE));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_MAGENTA = ITEMS.register("translucent_luminous_block_magenta",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.MAGENTA));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_LIGHT_BLUE = ITEMS.register("translucent_luminous_block_light_blue",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIGHT_BLUE));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_YELLOW = ITEMS.register("translucent_luminous_block_yellow",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.YELLOW));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_LIME = ITEMS.register("translucent_luminous_block_lime",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIME));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_PINK = ITEMS.register("translucent_luminous_block_pink",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.PINK));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_GRAY = ITEMS.register("translucent_luminous_block_gray",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.GRAY));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_LIGHT_GRAY = ITEMS.register("translucent_luminous_block_light_gray",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.LIGHT_GRAY));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_CYAN = ITEMS.register("translucent_luminous_block_cyan",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.CYAN));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_PURPLE = ITEMS.register("translucent_luminous_block_purple",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.PURPLE));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_BLUE = ITEMS.register("translucent_luminous_block_blue",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BLUE));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_BROWN = ITEMS.register("translucent_luminous_block_brown",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BROWN));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_GREEN = ITEMS.register("translucent_luminous_block_green",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.GREEN));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_RED = ITEMS.register("translucent_luminous_block_red",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.RED));
    public static final Supplier<Item> TRANSLUCENT_LUMINOUS_BLOCK_BLACK = ITEMS.register("translucent_luminous_block_black",
        () -> new LuminousBlockItem(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get(), new Item.Properties(), DyeColor.BLACK));

    // Stained Bricks
    public static final Supplier<Item> STAINED_BRICK_WHITE = ITEMS.register("stained_brick_white",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.WHITE));
    public static final Supplier<Item> STAINED_BRICK_ORANGE = ITEMS.register("stained_brick_orange",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.ORANGE));
    public static final Supplier<Item> STAINED_BRICK_MAGENTA = ITEMS.register("stained_brick_magenta",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.MAGENTA));
    public static final Supplier<Item> STAINED_BRICK_LIGHT_BLUE = ITEMS.register("stained_brick_light_blue",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIGHT_BLUE));
    public static final Supplier<Item> STAINED_BRICK_YELLOW = ITEMS.register("stained_brick_yellow",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.YELLOW));
    public static final Supplier<Item> STAINED_BRICK_LIME = ITEMS.register("stained_brick_lime",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIME));
    public static final Supplier<Item> STAINED_BRICK_PINK = ITEMS.register("stained_brick_pink",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.PINK));
    public static final Supplier<Item> STAINED_BRICK_GRAY = ITEMS.register("stained_brick_gray",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.GRAY));
    public static final Supplier<Item> STAINED_BRICK_LIGHT_GRAY = ITEMS.register("stained_brick_light_gray",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIGHT_GRAY));
    public static final Supplier<Item> STAINED_BRICK_CYAN = ITEMS.register("stained_brick_cyan",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.CYAN));
    public static final Supplier<Item> STAINED_BRICK_PURPLE = ITEMS.register("stained_brick_purple",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.PURPLE));
    public static final Supplier<Item> STAINED_BRICK_BLUE = ITEMS.register("stained_brick_blue",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.BLUE));
    public static final Supplier<Item> STAINED_BRICK_BROWN = ITEMS.register("stained_brick_brown",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.BROWN));
    public static final Supplier<Item> STAINED_BRICK_GREEN = ITEMS.register("stained_brick_green",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.GREEN));
    public static final Supplier<Item> STAINED_BRICK_RED = ITEMS.register("stained_brick_red",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.RED));
    public static final Supplier<Item> STAINED_BRICK_BLACK = ITEMS.register("stained_brick_black",
        () -> new StainedBrickItem(ModBlocks.STAINED_BRICK.get(), new Item.Properties(), DyeColor.BLACK));

    // Luminous Stained Bricks  
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_WHITE = ITEMS.register("luminous_stained_brick_white",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.WHITE));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_ORANGE = ITEMS.register("luminous_stained_brick_orange",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.ORANGE));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_MAGENTA = ITEMS.register("luminous_stained_brick_magenta",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.MAGENTA));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_LIGHT_BLUE = ITEMS.register("luminous_stained_brick_light_blue",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIGHT_BLUE));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_YELLOW = ITEMS.register("luminous_stained_brick_yellow",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.YELLOW));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_LIME = ITEMS.register("luminous_stained_brick_lime",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIME));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_PINK = ITEMS.register("luminous_stained_brick_pink",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.PINK));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_GRAY = ITEMS.register("luminous_stained_brick_gray",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.GRAY));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_LIGHT_GRAY = ITEMS.register("luminous_stained_brick_light_gray",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.LIGHT_GRAY));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_CYAN = ITEMS.register("luminous_stained_brick_cyan",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.CYAN));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_PURPLE = ITEMS.register("luminous_stained_brick_purple",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.PURPLE));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_BLUE = ITEMS.register("luminous_stained_brick_blue",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.BLUE));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_BROWN = ITEMS.register("luminous_stained_brick_brown",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.BROWN));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_GREEN = ITEMS.register("luminous_stained_brick_green",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.GREEN));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_RED = ITEMS.register("luminous_stained_brick_red",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.RED));
    public static final Supplier<Item> LUMINOUS_STAINED_BRICK_BLACK = ITEMS.register("luminous_stained_brick_black",
        () -> new StainedBrickItem(ModBlocks.LUMINOUS_STAINED_BRICK.get(), new Item.Properties(), DyeColor.BLACK));

    // Creative tab
    public static final Supplier<CreativeModeTab> RT_CREATIVE_TAB = CREATIVE_MODE_TABS.register("randomthings", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.randomthings"))
        .icon(() -> new ItemStack(FERTILIZED_DIRT.get()))
        .displayItems((parameters, output) -> {
            output.accept(FERTILIZED_DIRT.get());
            output.accept(RAINBOW_LAMP.get());
            output.accept(RAIN_SHIELD.get());
            output.accept(SLIME_CUBE.get());
            output.accept(SUPER_LUBRICENT_STONE.get());
            output.accept(COMPRESSED_SLIME_BLOCK.get());
            output.accept(ADVANCED_REDSTONE_TORCH.get());
            output.accept(ADVANCED_REDSTONE_REPEATER.get());
            output.accept(ANALOG_EMITTER.get());
            output.accept(SIDED_REDSTONE.get());
            output.accept(BLOCK_BREAKER.get());
            output.accept(SOUND_DAMPENER.get());
            output.accept(BLOCK_DESTABILIZER.get());
            output.accept(IRON_DROPPER.get());
            output.accept(CHAT_DETECTOR.get());
            output.accept(GLOBAL_CHAT_DETECTOR.get());
            output.accept(PEACE_CANDLE.get());
            output.accept(POTION_VAPORIZER.get());
            output.accept(CONTACT_BUTTON.get());
            output.accept(CONTACT_LEVER.get());
            output.accept(BLOCK_OF_STICKS.get());
            output.accept(BLOCK_OF_STICKS_RETURNING.get());
            output.accept(PLATFORM_OAK.get());
            output.accept(PLATFORM_SPRUCE.get());
            output.accept(PLATFORM_BIRCH.get());
            output.accept(PLATFORM_JUNGLE.get());
            output.accept(PLATFORM_ACACIA.get());
            output.accept(PLATFORM_DARKOAK.get());
            output.accept(BLOOD_ROSE.get());
            output.accept(BLOOD_ROSE_PETAL.get());
            output.accept(GLOWING_MUSHROOM.get());
            output.accept(POSITION_FILTER.get());
            output.accept(ITEM_FILTER.get());
            output.accept(ENTITY_FILTER.get());

            // Lava Charm needs to be added with default charge
            ItemStack lavaCharm = new ItemStack(LAVA_CHARM.get());
            lavaCharm.set(ModDataComponents.LAVA_CHARM_CHARGE.get(), LavaCharmItem.MAX_CHARGE);
            lavaCharm.set(ModDataComponents.LAVA_CHARM_COOLDOWN.get(), 0);
            output.accept(lavaCharm);

            output.accept(OBSIDIAN_SKULL.get());
            output.accept(WATER_WALKING_BOOTS.get());
            output.accept(OBSIDIAN_WATER_WALKING_BOOTS.get());

            // Lava Waders need to be added with default charge
            ItemStack lavaWaders = new ItemStack(LAVA_WADERS.get());
            lavaWaders.set(ModDataComponents.LAVA_CHARM_CHARGE.get(), LavaCharmItem.MAX_CHARGE);
            lavaWaders.set(ModDataComponents.LAVA_CHARM_COOLDOWN.get(), 0);
            output.accept(lavaWaders);

            output.accept(MAGIC_HOOD.get());
            output.accept(SUPER_LUBRICENT_TINCTURE.get());
            output.accept(SUPER_LUBRICENT_BOOTS.get());
            output.accept(SUMMONING_PENDULUM.get());
            output.accept(ECTOPLASM.get());
            output.accept(SPECTRE_INGOT.get());
            output.accept(SPECTRE_ANCHOR.get());
            output.accept(SPECTRE_ILLUMINATOR.get());
            output.accept(BLACKOUT_POWDER.get());
            output.accept(SPECTRE_KEY.get());
            output.accept(SPECTRE_BLOCK.get());
            // Don't add SPECTRE_CORE to creative tab - it's generated by the dimension
            output.accept(BIOME_STONE_SMOOTH.get());
            output.accept(BIOME_STONE_COBBLE.get());
            output.accept(BIOME_STONE_BRICK.get());
            output.accept(BIOME_STONE_CRACKED.get());
            output.accept(BIOME_STONE_CHISELED.get());
            output.accept(BIOME_GLASS.get());
            output.accept(LAPIS_GLASS.get());
            output.accept(LAPIS_LAMP.get());
            // Add diaphanous block with default stone appearance
            ItemStack diaphanousStack = new ItemStack(DIAPHANOUS_BLOCK.get());
            diaphanousStack.set(ModDataComponents.DIAPHANOUS_BLOCK_STATE.get(), 
                              net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "stone"));
            diaphanousStack.set(ModDataComponents.DIAPHANOUS_INVERTED.get(), false);
            output.accept(diaphanousStack);
            // Add example Biome Crystal (Plains biome for creative tab)
            output.accept(BiomeCrystalItem.createForBiome(Biomes.PLAINS));
            // Bean System Items
            output.accept(BEAN.get());
            output.accept(LESSERBEAN.get());
            output.accept(MAGICBEAN.get());
            output.accept(BEANSTEW.get());
            output.accept(GOLDEN_EGG.get());
            output.accept(BEANSPROUT.get());
            output.accept(BEANSTALK.get());
            output.accept(SPECIALBEANSTALK.get());
            output.accept(BEANPOD.get());
            output.accept(NATURE_CORE.get());
            output.accept(PLANT_CHEST.get());
            output.accept(ID_CARD.get());
            output.accept(DIVINING_ROD_COAL.get());
            output.accept(DIVINING_ROD_IRON.get());
            output.accept(DIVINING_ROD_GOLD.get());
            output.accept(DIVINING_ROD_LAPIS.get());
            output.accept(DIVINING_ROD_REDSTONE.get());
            output.accept(DIVINING_ROD_EMERALD.get());
            output.accept(DIVINING_ROD_DIAMOND.get());
            output.accept(DIVINING_ROD_VANILLA.get());
            // Spectre Tools
            output.accept(SPECTRE_PICKAXE.get());
            output.accept(SPECTRE_SWORD.get());
            output.accept(SPECTRE_SHOVEL.get());
            output.accept(SPECTRE_AXE.get());
            output.accept(EMERALD_COMPASS.get());
            output.accept(GOLDEN_COMPASS.get());
            output.accept(BLAZE_AND_STEEL.get());

            // Redstone Activator with default duration (20 ticks)
            ItemStack redstoneActivator = new ItemStack(REDSTONE_ACTIVATOR.get());
            redstoneActivator.set(ModDataComponents.REDSTONE_ACTIVATOR_DURATION.get(), 1);
            output.accept(redstoneActivator);

            output.accept(REDSTONE_REMOTE.get());

            output.accept(SOUND_PATTERN.get());
            output.accept(SOUND_RECORDER.get());
            output.accept(PORTABLE_SOUND_DAMPENER.get());

            output.accept(CHUNK_ANALYZER.get());
            output.accept(ECLIPSED_CLOCK.get());
            output.accept(TIME_IN_A_BOTTLE.get());
            output.accept(ESCAPE_ROPE.get());
            // Ender Bridge System
            output.accept(STABLE_ENDER_PEARL.get());
            output.accept(PORTKEY.get());
            output.accept(ENDER_BUCKET.get());
            output.accept(REINFORCED_ENDER_BUCKET.get());
            output.accept(ENDER_LETTER.get());
            output.accept(ENDER_MAILBOX.get());
            output.accept(ENDER_ANCHOR.get());
            output.accept(ENDER_BRIDGE.get());
            output.accept(PRISMARINE_ENDER_BRIDGE.get());
            output.accept(FLUID_DISPLAY.get());
            output.accept(IGNITER.get());
            output.accept(INVENTORY_TESTER.get());
            output.accept(LIGHT_REDIRECTOR.get());
            output.accept(NOTIFICATION_INTERFACE.get());
            output.accept(PLAYER_INTERFACE.get());
            output.accept(QUARTZ_GLASS.get());
            output.accept(TRIGGER_GLASS.get());
            output.accept(QUARTZ_LAMP.get());
            output.accept(PITCHER_PLANT.get());
            output.accept(LOTUS_SEEDS.get());
            output.accept(LOTUS_BLOSSOM.get());
            // Luminous Blocks
            output.accept(LUMINOUS_POWDER.get());

            // Rune System - DON'T add rune_dust here, it gets auto-added
            // Players can craft colored versions from white rune dust
            output.accept(RUNE_PATTERN.get());
            output.accept(RUNE_BASE.get());

            output.accept(LUMINOUS_BLOCK_WHITE.get());
            output.accept(LUMINOUS_BLOCK_ORANGE.get());
            output.accept(LUMINOUS_BLOCK_MAGENTA.get());
            output.accept(LUMINOUS_BLOCK_LIGHT_BLUE.get());
            output.accept(LUMINOUS_BLOCK_YELLOW.get());
            output.accept(LUMINOUS_BLOCK_LIME.get());
            output.accept(LUMINOUS_BLOCK_PINK.get());
            output.accept(LUMINOUS_BLOCK_GRAY.get());
            output.accept(LUMINOUS_BLOCK_LIGHT_GRAY.get());
            output.accept(LUMINOUS_BLOCK_CYAN.get());
            output.accept(LUMINOUS_BLOCK_PURPLE.get());
            output.accept(LUMINOUS_BLOCK_BLUE.get());
            output.accept(LUMINOUS_BLOCK_BROWN.get());
            output.accept(LUMINOUS_BLOCK_GREEN.get());
            output.accept(LUMINOUS_BLOCK_RED.get());
            output.accept(LUMINOUS_BLOCK_BLACK.get());
            // Translucent Luminous Blocks
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_WHITE.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_ORANGE.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_MAGENTA.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_LIGHT_BLUE.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_YELLOW.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_LIME.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_PINK.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_GRAY.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_LIGHT_GRAY.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_CYAN.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_PURPLE.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_BLUE.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_BROWN.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_GREEN.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_RED.get());
            output.accept(TRANSLUCENT_LUMINOUS_BLOCK_BLACK.get());
            // Stained Bricks
            output.accept(STAINED_BRICK_WHITE.get());
            output.accept(STAINED_BRICK_ORANGE.get());
            output.accept(STAINED_BRICK_MAGENTA.get());
            output.accept(STAINED_BRICK_LIGHT_BLUE.get());
            output.accept(STAINED_BRICK_YELLOW.get());
            output.accept(STAINED_BRICK_LIME.get());
            output.accept(STAINED_BRICK_PINK.get());
            output.accept(STAINED_BRICK_GRAY.get());
            output.accept(STAINED_BRICK_LIGHT_GRAY.get());
            output.accept(STAINED_BRICK_CYAN.get());
            output.accept(STAINED_BRICK_PURPLE.get());
            output.accept(STAINED_BRICK_BLUE.get());
            output.accept(STAINED_BRICK_BROWN.get());
            output.accept(STAINED_BRICK_GREEN.get());
            output.accept(STAINED_BRICK_RED.get());
            output.accept(STAINED_BRICK_BLACK.get());
            // Luminous Stained Bricks
            output.accept(LUMINOUS_STAINED_BRICK_WHITE.get());
            output.accept(LUMINOUS_STAINED_BRICK_ORANGE.get());
            output.accept(LUMINOUS_STAINED_BRICK_MAGENTA.get());
            output.accept(LUMINOUS_STAINED_BRICK_LIGHT_BLUE.get());
            output.accept(LUMINOUS_STAINED_BRICK_YELLOW.get());
            output.accept(LUMINOUS_STAINED_BRICK_LIME.get());
            output.accept(LUMINOUS_STAINED_BRICK_PINK.get());
            output.accept(LUMINOUS_STAINED_BRICK_GRAY.get());
            output.accept(LUMINOUS_STAINED_BRICK_LIGHT_GRAY.get());
            output.accept(LUMINOUS_STAINED_BRICK_CYAN.get());
            output.accept(LUMINOUS_STAINED_BRICK_PURPLE.get());
            output.accept(LUMINOUS_STAINED_BRICK_BLUE.get());
            output.accept(LUMINOUS_STAINED_BRICK_BROWN.get());
            output.accept(LUMINOUS_STAINED_BRICK_GREEN.get());
            output.accept(LUMINOUS_STAINED_BRICK_RED.get());
            output.accept(LUMINOUS_STAINED_BRICK_BLACK.get());
        })
        .build());

    private static Color[] getAllColors() {
        return new Color[]{
            new Color(20, 20, 20, 50),      // coal
            new Color(211, 180, 159, 50),   // iron
            new Color(246, 233, 80, 50),    // gold
            new Color(5, 45, 150, 50),      // lapis
            new Color(211, 1, 1, 50),       // redstone
            new Color(0, 220, 0, 50),       // emerald
            new Color(87, 221, 229, 50)     // diamond
        };
    }

    private static String[] getAllTags() {
        return new String[]{
            "minecraft:coal_ores",
            "minecraft:iron_ores", 
            "minecraft:gold_ores",
            "minecraft:lapis_ores",
            "minecraft:redstone_ores",
            "minecraft:emerald_ores",
            "minecraft:diamond_ores"
        };
    }
}