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
 * Lava Waders - The ultimate boots combining all protection effects.
 *
 * Features:
 * - Walk on water surfaces (Water Walking Boots effect)
 * - Walk on lava surfaces (NEW - does not consume charges)
 * - Fire damage immunity (Obsidian Skull effect)
 * - Lava damage immunity with charge system (Lava Charm effect)
 * - Indestructible (no durability)
 * - Chain armor protection level
 *
 * Charge System:
 * - Max 200 charges (10 seconds of lava protection)
 * - Recharges when out of lava
 * - Walking ON lava surface does NOT consume charges
 * - Only submerging IN lava consumes charges
 */
public class LavaWadersItem extends ArmorItem {

    public LavaWadersItem(Properties properties) {
        super(ModArmorMaterials.LAVA_WADERS, Type.BOOTS, properties
            .rarity(Rarity.EPIC) // Epic rarity (purple name)
            .durability(0)); // Indestructible
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);

        // Add tooltip from language file
        tooltipComponents.add(
            Component.translatable("tooltip.randomthings.lava_waders")
                .withStyle(ChatFormatting.GRAY)
        );

        // Show charge status if data components are present
        if (stack.has(ModDataComponents.LAVA_CHARM_CHARGE.get())) {
            int charge = stack.get(ModDataComponents.LAVA_CHARM_CHARGE.get());
            tooltipComponents.add(
                Component.literal("Charges: " + charge + "/200")
                    .withStyle(ChatFormatting.GOLD)
            );
        }
    }
}
