package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Spectre Anchor. (Its 1.21.1 effect — keeping anchored items in the inventory on death — needs a
 * drops-modification event Architectury doesn't expose cross-loader; deferred. The item is present
 * and craftable in the meantime.)
 */
public class SpectreAnchorItem extends Item {
    public SpectreAnchorItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.randomthings.spectre_anchor").withStyle(ChatFormatting.GRAY));
    }
}
