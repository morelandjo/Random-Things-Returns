package lumien.randomthings.block;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModConstants.MOD_ID);

    public static final Supplier<Block> FERTILIZED_DIRT = BLOCKS.register("fertilized_dirt", 
        () -> new FertilizedDirtBlock());

    public static final Supplier<Block> RAINBOW_LAMP = BLOCKS.register("rainbow_lamp", 
        () -> new RainbowLampBlock());

    public static final Supplier<Block> ADVANCED_REDSTONE_TORCH = BLOCKS.register("advanced_redstone_torch", 
        () -> new AdvancedRedstoneTorchBlock());

    public static final Supplier<Block> ADVANCED_WALL_REDSTONE_TORCH = BLOCKS.register("advanced_redstone_wall_torch", 
        () -> new AdvancedRedstoneWallTorchBlock());

    public static final Supplier<Block> SUPER_LUBRICENT_STONE = BLOCKS.register("super_lubricent_stone", 
        () -> new SuperLubricentStoneBlock());

    public static final Supplier<Block> BLOCK_OF_STICKS = BLOCKS.register("block_of_sticks", 
        () -> new SticksBlock(false));

    public static final Supplier<Block> BLOCK_OF_STICKS_RETURNING = BLOCKS.register("block_of_sticks_returning", 
        () -> new SticksBlock(true));

    public static final Supplier<Block> PLATFORM_OAK = BLOCKS.register("platform_oak", 
        () -> new PlatformBlock());

    public static final Supplier<Block> PLATFORM_SPRUCE = BLOCKS.register("platform_spruce", 
        () -> new PlatformBlock());

    public static final Supplier<Block> PLATFORM_BIRCH = BLOCKS.register("platform_birch", 
        () -> new PlatformBlock());

    public static final Supplier<Block> PLATFORM_JUNGLE = BLOCKS.register("platform_jungle", 
        () -> new PlatformBlock());

    public static final Supplier<Block> PLATFORM_ACACIA = BLOCKS.register("platform_acacia", 
        () -> new PlatformBlock());

    public static final Supplier<Block> PLATFORM_DARKOAK = BLOCKS.register("platform_darkoak", 
        () -> new PlatformBlock());

    public static final Supplier<Block> BLOOD_ROSE = BLOCKS.register("blood_rose", 
        () -> new BloodRoseBlock());


    public static final Supplier<Block> ADVANCED_REDSTONE_REPEATER = BLOCKS.register("advanced_redstone_repeater", 
        () -> new AdvancedRedstoneRepeaterBlock(false));

    public static final Supplier<Block> ADVANCED_REDSTONE_REPEATER_POWERED = BLOCKS.register("advanced_redstone_repeater_powered", 
        () -> new AdvancedRedstoneRepeaterBlock(true));

    public static final Supplier<Block> ANALOG_EMITTER = BLOCKS.register("analog_emitter", 
        () -> new AnalogEmitterBlock());

    // Biome Blocks
    public static final Supplier<Block> BIOME_STONE = BLOCKS.register("biome_stone", 
        () -> new BiomeStoneBlock());
    
    public static final Supplier<Block> BIOME_GLASS = BLOCKS.register("biome_glass", 
        () -> new BiomeGlassBlock());
    
    public static final Supplier<Block> LAPIS_GLASS = BLOCKS.register("lapis_glass", 
        () -> new LapisGlassBlock());

    public static final Supplier<Block> BLOCK_BREAKER = BLOCKS.register("block_breaker", 
        () -> new BlockBreakerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(3.5F)
            .sound(SoundType.STONE)));

    public static final Supplier<Block> BLOCK_DESTABILIZER = BLOCKS.register("block_destabilizer", 
        () -> new BlockDestabilizerBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(5.0F)
            .sound(SoundType.STONE)));

    public static final Supplier<Block> CHAT_DETECTOR = BLOCKS.register("chat_detector", 
        () -> new ChatDetectorBlock());

    public static final Supplier<Block> GLOBAL_CHAT_DETECTOR = BLOCKS.register("global_chat_detector", 
        () -> new GlobalChatDetectorBlock());

    public static final Supplier<Block> COMPRESSED_SLIME_BLOCK = BLOCKS.register("compressed_slime_block", 
        () -> new CompressedSlimeBlock());

    public static final Supplier<Block> CONTACT_BUTTON = BLOCKS.register("contact_button", 
        () -> new ContactButtonBlock());

    public static final Supplier<Block> IRON_DROPPER = BLOCKS.register("iron_dropper", 
        () -> new IronDropperBlock());

    public static final Supplier<Block> CONTACT_LEVER = BLOCKS.register("contact_lever", 
        () -> new ContactLeverBlock());
    
    public static final Supplier<Block> DIAPHANOUS_BLOCK = BLOCKS.register("diaphanous_block", 
        () -> new DiaphanousBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.NONE)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .noOcclusion()));

    // Ender Bridge System
    public static final Supplier<Block> ENDER_ANCHOR = BLOCKS.register("ender_anchor", 
        () -> new EnderAnchorBlock());

    public static final Supplier<Block> ENDER_BRIDGE = BLOCKS.register("ender_bridge", 
        () -> new EnderBridgeBlock());

    public static final Supplier<Block> PRISMARINE_ENDER_BRIDGE = BLOCKS.register("prismarine_ender_bridge", 
        () -> new PrismarineEnderBridgeBlock());
        
    public static final Supplier<Block> FLUID_DISPLAY = BLOCKS.register("fluid_display", 
        () -> new FluidDisplayBlock());

    public static final Supplier<Block> IGNITER = BLOCKS.register("igniter", 
        () -> new IgniterBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .requiresCorrectToolForDrops()
            .strength(1.5F)
            .sound(SoundType.STONE)));

    public static final Supplier<Block> INVENTORY_TESTER = BLOCKS.register("inventory_tester", 
        () -> new InventoryTesterBlock());
}