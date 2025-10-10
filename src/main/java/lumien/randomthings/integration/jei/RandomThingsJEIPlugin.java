package lumien.randomthings.integration.jei;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@JeiPlugin
public class RandomThingsJEIPlugin implements IModPlugin {
    
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "jei_plugin");
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // Register custom recipe categories here when needed
    }
    
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Register custom recipes here when needed
    }
    
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // Register crafting table as catalyst for all our crafting recipes
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Add our blocks as information displays
        // Players can see these blocks' uses and crafting recipes
        
        // Redstone blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_REDSTONE_REPEATER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ADVANCED_REDSTONE_TORCH.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ANALOG_EMITTER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SIDED_REDSTONE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        // registration.addRecipeCatalyst(new ItemStack(ModBlocks.CHAT_DETECTOR.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        // registration.addRecipeCatalyst(new ItemStack(ModBlocks.ONLINE_DETECTOR.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        // registration.addRecipeCatalyst(new ItemStack(ModBlocks.GLOBAL_CHAT_DETECTOR.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PEACE_CANDLE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POTION_VAPORIZER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CONTACT_BUTTON.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CONTACT_LEVER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.IGNITER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INVENTORY_TESTER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        // registration.addRecipeCatalyst(new ItemStack(ModBlocks.NOTIFICATION_INTERFACE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        // registration.addRecipeCatalyst(new ItemStack(ModBlocks.BASIC_REDSTONE_INTERFACE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Utility blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_BREAKER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_DESTABILIZER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.IRON_DROPPER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Decorative blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RAINBOW_LAMP.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RAIN_SHIELD.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SLIME_CUBE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.QUARTZ_LAMP.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_OF_STICKS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BLOCK_OF_STICKS_RETURNING.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Functional blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FERTILIZED_DIRT.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.SUPER_LUBRICENT_STONE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Platform blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_OAK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_SPRUCE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_BIRCH.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_JUNGLE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_ACACIA.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PLATFORM_DARKOAK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Plants
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GLOWING_MUSHROOM.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Biome blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIOME_STONE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BIOME_GLASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LAPIS_GLASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.QUARTZ_GLASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TRIGGER_GLASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Fluid Display - decorative fluid texture block
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.FLUID_DISPLAY.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Luminous blocks - decorative light emitting blocks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LUMINOUS_BLOCK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.TRANSLUCENT_LUMINOUS_BLOCK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Stained Bricks - colored decorative bricks
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.STAINED_BRICK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LUMINOUS_STAINED_BRICK.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        
        // Tools
        registration.addRecipeCatalyst(new ItemStack(ModItems.EMERALD_COMPASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.GOLDEN_COMPASS.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BLAZE_AND_STEEL.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.REDSTONE_ACTIVATOR.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.REDSTONE_REMOTE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Lava and Fire Protection Items
        registration.addRecipeCatalyst(new ItemStack(ModItems.OBSIDIAN_SKULL.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.CHUNK_ANALYZER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ESCAPE_ROPE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.POSITION_FILTER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ITEM_FILTER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ENTITY_FILTER.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Ender Bucket System
        registration.addRecipeCatalyst(new ItemStack(ModItems.STABLE_ENDER_PEARL.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.PORTKEY.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.ENDER_BUCKET.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.REINFORCED_ENDER_BUCKET.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Rune System
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RUNE_BASE.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.RUNE_DUST.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(new ItemStack(ModItems.RUNE_PATTERN.get()), mezz.jei.api.constants.RecipeTypes.CRAFTING);

        // Special blocks (Note: BLOOD_ROSE is not added as it's a dangerous block that spreads)
    }
    
    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        // Called when JEI runtime is available
        // Can be used to interact with JEI directly
    }
}