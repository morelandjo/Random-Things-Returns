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
 * Magic Hood - A rare helmet that provides visual stealth effects.
 *
 * Effects:
 * - Hides player nameplate in multiplayer (even when not crouching)
 * - Suppresses potion particle effects
 *
 * Properties:
 * - Chain armor protection level (2 armor points for helmet)
 * - Indestructible (no durability)
 * - Rare rarity
 *
 * Loot:
 * - Found in dungeon chests (5% chance)
 * - Found in village blacksmith chests (15% chance)
 */
public class MagicHoodItem extends ArmorItem {

    public MagicHoodItem(Properties properties) {
        super(ModArmorMaterials.MAGIC_HOOD, Type.HELMET, properties
            .rarity(Rarity.RARE) // Set rarity to RARE
            .durability(0)); // Indestructible - 0 durability
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);

        // Add tooltip from language file
        tooltipComponents.add(
            Component.translatable("tooltip.randomthings.magic_hood")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
