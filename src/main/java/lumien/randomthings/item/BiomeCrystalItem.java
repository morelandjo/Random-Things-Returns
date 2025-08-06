package lumien.randomthings.item;

import lumien.randomthings.util.BiomeColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

public class BiomeCrystalItem extends Item {
    
    public BiomeCrystalItem(Properties properties) {
        super(properties);
    }
    
    public static ItemStack createForBiome(ResourceKey<Biome> biome) {
        ItemStack stack = new ItemStack(ModItems.BIOME_CRYSTAL.get());
        setBiome(stack, biome);
        return stack;
    }
    
    public static void setBiome(ItemStack stack, ResourceKey<Biome> biome) {
        stack.set(ModDataComponents.BIOME_CRYSTAL_BIOME.get(), biome.location());
    }
    
    @Nullable
    public static ResourceKey<Biome> getBiome(ItemStack stack) {
        ResourceLocation biomeLocation = stack.get(ModDataComponents.BIOME_CRYSTAL_BIOME.get());
        if (biomeLocation != null) {
            return ResourceKey.create(Registries.BIOME, biomeLocation);
        }
        return null;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceKey<Biome> biome = getBiome(stack);
        if (biome != null && context.level() != null) {
            Holder<Biome> biomeHolder = context.level().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
            String biomeName = biome.location().getPath().replace("_", " ");
            biomeName = biomeName.substring(0, 1).toUpperCase() + biomeName.substring(1);
            
            tooltip.add(Component.literal("Biome: " + biomeName).withStyle(ChatFormatting.GRAY));
            
            // Add enchanted effect for magical biomes
            if (BiomeColorUtils.isMagicalBiome(biomeHolder)) {
                tooltip.add(Component.literal("✨ Magical").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        tooltip.add(Component.literal("Used to craft Biome Blocks").withStyle(ChatFormatting.DARK_GRAY));
    }
    
    @Override
    public Component getName(ItemStack stack) {
        ResourceKey<Biome> biome = getBiome(stack);
        if (biome != null) {
            String biomeName = biome.location().getPath().replace("_", " ");
            biomeName = biomeName.substring(0, 1).toUpperCase() + biomeName.substring(1);
            return Component.literal(biomeName + " " + super.getName(stack).getString());
        }
        return super.getName(stack);
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        ResourceKey<Biome> biome = getBiome(stack);
        if (biome != null) {
            // Add enchanted glint to magical biomes
            return isMagicalBiome(biome);
        }
        return false;
    }
    
    // Helper method for isMagicalBiome check with ResourceKey
    private static boolean isMagicalBiome(ResourceKey<Biome> biome) {
        String biomeName = biome.location().getPath();
        return biomeName.contains("mushroom") || 
               biomeName.contains("warped") || 
               biomeName.contains("crimson") ||
               biomeName.contains("end") ||
               biomeName.contains("cherry");
    }
}