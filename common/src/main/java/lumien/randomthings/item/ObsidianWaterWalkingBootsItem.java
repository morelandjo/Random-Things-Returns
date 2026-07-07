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

/** Boots that combine Water Walking with the Obsidian Skull's chance-based fire immunity. */
public class ObsidianWaterWalkingBootsItem extends ArmorItem {
    public ObsidianWaterWalkingBootsItem(Properties properties) {
        super(ModArmorMaterials.OBSIDIAN_WATER_WALKING, Type.BOOTS, properties.rarity(Rarity.RARE).durability(0));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.randomthings.obsidian_water_walking_boots").withStyle(ChatFormatting.GRAY));
    }
}
