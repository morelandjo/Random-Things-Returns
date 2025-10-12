package lumien.randomthings.item;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.common.Tags;

public class SpectreToolMaterial {

    // Spectre tools: Better than diamond in all aspects
    // Durability: 2000 (vs Diamond's 1561)
    // Mining Speed: 8.0f (vs Diamond's 8.0f)
    // Attack Damage: 3.0f (vs Diamond's 3.0f)
    // Harvest Level: 3 (same as diamond)
    // Enchantability: 22 (vs Diamond's 10)
    // Repair: Ectoplasm

    public static final Tier SPECTRE = new SimpleTier(
        // Incorrect for tool tag - blocks this tool cannot mine
        Tags.Blocks.NEEDS_NETHERITE_TOOL,
        // Uses (durability)
        2000,
        // Speed
        8.0f,
        // Attack damage bonus
        3.0f,
        // Enchantability
        22,
        // Repair ingredient
        () -> Ingredient.of(ModItems.ECTOPLASM.get())
    );
}
