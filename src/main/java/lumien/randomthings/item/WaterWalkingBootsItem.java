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
 * Water Walking Boots allow the player to walk on water surfaces.
 * - Found in Ocean Monument treasure room chests
 * - Indestructible (no durability)
 * - Chain armor protection level
 * - Hold shift to enter water normally
 * - Can jump out of water while on surface
 */
public class WaterWalkingBootsItem extends ArmorItem {

    public WaterWalkingBootsItem(Properties properties) {
        super(ModArmorMaterials.WATER_WALKING, Type.BOOTS, properties
            .rarity(Rarity.RARE) // Set rarity via properties
            .durability(0)); // Indestructible - 0 durability
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);

        // Add tooltip from language file
        tooltipComponents.add(
            Component.translatable("tooltip.randomthings.water_walking_boots")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
