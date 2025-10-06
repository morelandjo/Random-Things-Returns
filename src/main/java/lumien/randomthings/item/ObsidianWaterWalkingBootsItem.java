package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Obsidian Water Walking Boots combine the effects of Water Walking Boots and Obsidian Skull.
 * - Walk on water surfaces
 * - Chance-based fire damage immunity
 * - Indestructible (no durability)
 * - Chain armor protection level
 * - Hold shift to enter water normally
 */
public class ObsidianWaterWalkingBootsItem extends ArmorItem {

    public ObsidianWaterWalkingBootsItem(Properties properties) {
        super(ModArmorMaterials.OBSIDIAN_WATER_WALKING, Type.BOOTS, properties
            .rarity(Rarity.RARE) // Set rarity via properties
            .durability(0)); // Indestructible - 0 durability
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);

        // Add tooltip from language file
        tooltipComponents.add(
            Component.translatable("tooltip.randomthings.obsidian_water_walking_boots")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
