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

/** Water Walking Boots — walk on water surfaces (sneak to enter water). Behaviour in {@code BootsHandler}. */
public class WaterWalkingBootsItem extends ArmorItem {

    public WaterWalkingBootsItem(Properties properties) {
        super(ModArmorMaterials.WATER_WALKING, Type.BOOTS, properties.rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.randomthings.water_walking_boots").withStyle(ChatFormatting.GRAY));
    }
}
