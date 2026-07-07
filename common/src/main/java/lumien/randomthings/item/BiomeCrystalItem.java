package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import java.util.List;

/** Holds a captured biome; an ingredient for the Biome Stone / Biome Glass recipes. */
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
        RTNbt.setResourceLocation(stack, RTDataKeys.BIOME_CRYSTAL_BIOME, biome.location());
    }

    @Nullable
    public static ResourceKey<Biome> getBiome(ItemStack stack) {
        ResourceLocation loc = RTNbt.getResourceLocation(stack, RTDataKeys.BIOME_CRYSTAL_BIOME);
        return loc == null ? null : ResourceKey.create(Registries.BIOME, loc);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ResourceKey<Biome> biome = getBiome(stack);
        if (biome != null) {
            tooltip.add(Component.literal("Biome: " + prettyName(biome)).withStyle(ChatFormatting.GRAY));
            if (isMagicalBiome(biome)) {
                tooltip.add(Component.literal("✨ Magical").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        tooltip.add(Component.literal("Used to craft Biome Blocks").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceKey<Biome> biome = getBiome(stack);
        if (biome != null) {
            return Component.literal(prettyName(biome) + " " + super.getName(stack).getString());
        }
        return super.getName(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        ResourceKey<Biome> biome = getBiome(stack);
        return biome != null && isMagicalBiome(biome);
    }

    private static String prettyName(ResourceKey<Biome> biome) {
        String name = biome.location().getPath().replace("_", " ");
        return name.isEmpty() ? name : name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static boolean isMagicalBiome(ResourceKey<Biome> biome) {
        String n = biome.location().getPath();
        return n.contains("mushroom") || n.contains("warped") || n.contains("crimson") || n.contains("end") || n.contains("cherry");
    }
}
