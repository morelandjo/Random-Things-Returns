package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Spectre pickaxe. (The 1.21.1 +3-block reach bonus is Forge-only in 1.20.1 and is omitted.) */
public class SpectrePickaxeItem extends PickaxeItem {
    public SpectrePickaxeItem(Properties properties) {
        super(SpectreToolMaterial.SPECTRE, 1, -2.8F, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.randomthings.spectre_pickaxe").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
