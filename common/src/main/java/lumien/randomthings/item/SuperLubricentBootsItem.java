package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Super Lubricent Boots — frictionless movement (sneak to disable). Behaviour in {@code BootsHandler}. */
public class SuperLubricentBootsItem extends ArmorItem {

    public SuperLubricentBootsItem(Properties properties) {
        super(ModArmorMaterials.SUPER_LUBRICENT, Type.BOOTS, properties.rarity(Rarity.UNCOMMON));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.randomthings.superlubricent_boots").withStyle(ChatFormatting.GRAY));
    }
}
