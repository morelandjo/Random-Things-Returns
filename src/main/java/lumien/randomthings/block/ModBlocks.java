package lumien.randomthings.block;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.block.Block;
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
}