package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Lava Waders — water-walking + lava-walking + lava-damage immunity (Lava Charm charge system).
 * Behaviour in {@code BootsHandler} (walking) and {@code LavaCharmHandler} (immunity + recharge).
 */
public class LavaWadersItem extends ArmorItem {

    public LavaWadersItem(Properties properties) {
        super(ModArmorMaterials.LAVA_WADERS, Type.BOOTS, properties.rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable("tooltip.randomthings.lava_waders").withStyle(ChatFormatting.GRAY));
        if (RTNbt.has(stack, RTDataKeys.LAVA_CHARM_CHARGE)) {
            int charge = RTNbt.getInt(stack, RTDataKeys.LAVA_CHARM_CHARGE, LavaCharmItem.MAX_CHARGE);
            tooltipComponents.add(Component.literal("Charges: " + charge + "/" + LavaCharmItem.MAX_CHARGE).withStyle(ChatFormatting.GOLD));
        }
    }
}
