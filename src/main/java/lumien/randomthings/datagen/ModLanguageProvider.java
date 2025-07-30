package lumien.randomthings.datagen;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, ModConstants.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        // Blocks
        addBlock(ModBlocks.ADVANCED_REDSTONE_TORCH, "Advanced Redstone Torch");
        addBlock(ModBlocks.BLOOD_ROSE, "Blood Rose");
        addBlock(ModBlocks.FERTILIZED_DIRT, "Fertilized Dirt");
        addBlock(ModBlocks.RAINBOW_LAMP, "Rainbow Lamp");
        addBlock(ModBlocks.SUPER_LUBRICENT_STONE, "Super Lubricent Stone");
        addBlock(ModBlocks.BLOCK_OF_STICKS, "Block of Sticks");
        addBlock(ModBlocks.BLOCK_OF_STICKS_RETURNING, "Block of Sticks (Returning)");
        
        // Platform blocks
        addBlock(ModBlocks.PLATFORM_OAK, "Oak Platform");
        addBlock(ModBlocks.PLATFORM_SPRUCE, "Spruce Platform");
        addBlock(ModBlocks.PLATFORM_BIRCH, "Birch Platform");
        addBlock(ModBlocks.PLATFORM_JUNGLE, "Jungle Platform");
        addBlock(ModBlocks.PLATFORM_ACACIA, "Acacia Platform");
        addBlock(ModBlocks.PLATFORM_DARKOAK, "Dark Oak Platform");

        // Items
        addItem(ModItems.BLOOD_ROSE_PETAL, "Blood Rose Petal");
        
        // Divining Rods
        addItem(ModItems.DIVINING_ROD_VANILLA, "Divining Rod");
        addItem(ModItems.DIVINING_ROD_COAL, "Divining Rod (Coal)");
        addItem(ModItems.DIVINING_ROD_IRON, "Divining Rod (Iron)");
        addItem(ModItems.DIVINING_ROD_GOLD, "Divining Rod (Gold)");
        addItem(ModItems.DIVINING_ROD_DIAMOND, "Divining Rod (Diamond)");
        addItem(ModItems.DIVINING_ROD_EMERALD, "Divining Rod (Emerald)");
        addItem(ModItems.DIVINING_ROD_LAPIS, "Divining Rod (Lapis)");
        addItem(ModItems.DIVINING_ROD_REDSTONE, "Divining Rod (Redstone)");

        // GUI
        add("gui.randomthings.advanced_redstone_torch.gs", "Green Signal");
        add("gui.randomthings.advanced_redstone_torch.rs", "Red Signal");

        // Creative Tab
        add("itemGroup.randomthings", "Random Things");

        // Tooltips and other text
        add("tooltip.randomthings.divining_rod", "Right-click to search for ores");
        add("tooltip.randomthings.platform", "Crouch to pass through");
        add("tooltip.randomthings.blood_rose", "Spreads to nearby grass");
        add("tooltip.randomthings.fertilized_dirt", "Accelerates plant growth");
        add("tooltip.randomthings.advanced_redstone_torch", "Configurable redstone output");
        add("tooltip.randomthings.rainbow_lamp", "Changes color based on redstone signal");
        add("tooltip.randomthings.super_lubricent_stone", "Very slippery surface");
        add("tooltip.randomthings.block_of_sticks_returning", "Returns to nearest player after 10 seconds");
    }
}