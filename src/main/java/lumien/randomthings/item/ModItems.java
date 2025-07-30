package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.ModConstants;
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

    // Regular Items
    public static final Supplier<Item> BLOOD_ROSE_PETAL = ITEMS.register("blood_rose_petal",
        () -> new Item(new Item.Properties()));

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

    // Creative tab
    public static final Supplier<CreativeModeTab> RT_CREATIVE_TAB = CREATIVE_MODE_TABS.register("randomthings", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.randomthings"))
        .icon(() -> new ItemStack(FERTILIZED_DIRT.get()))
        .displayItems((parameters, output) -> {
            output.accept(FERTILIZED_DIRT.get());
            output.accept(RAINBOW_LAMP.get());
            output.accept(SUPER_LUBRICENT_STONE.get());
            output.accept(ADVANCED_REDSTONE_TORCH.get());
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
            output.accept(DIVINING_ROD_COAL.get());
            output.accept(DIVINING_ROD_IRON.get());
            output.accept(DIVINING_ROD_GOLD.get());
            output.accept(DIVINING_ROD_LAPIS.get());
            output.accept(DIVINING_ROD_REDSTONE.get());
            output.accept(DIVINING_ROD_EMERALD.get());
            output.accept(DIVINING_ROD_DIAMOND.get());
            output.accept(DIVINING_ROD_VANILLA.get());
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