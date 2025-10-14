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
 * Super Lubricent Boots allow the player to move without friction, as if walking on ice.
 * - Craftable with iron boots and super lubricent tincture
 * - Iron armor protection level
 * - Standard durability
 * - Sneaking disables the effect
 */
public class SuperLubricentBootsItem extends ArmorItem {

    public SuperLubricentBootsItem(Properties properties) {
        super(ModArmorMaterials.SUPER_LUBRICENT, Type.BOOTS, properties
            .rarity(Rarity.UNCOMMON)); // Set rarity via properties
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, context, tooltipComponents, isAdvanced);

        // Add tooltip from language file
        tooltipComponents.add(
            Component.translatable("tooltip.randomthings.superlubricent_boots")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
