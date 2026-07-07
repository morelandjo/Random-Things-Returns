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

/**
 * A rare hood that hides the wearer's nameplate ({@code LivingEntityRendererMixin}) and suppresses
 * nearby potion particles ({@code ParticleEngineMixin}). Found in dungeon/toolsmith chests.
 */
public class MagicHoodItem extends ArmorItem {
    public MagicHoodItem(Properties properties) {
        super(ModArmorMaterials.MAGIC_HOOD, Type.HELMET, properties.rarity(Rarity.RARE).durability(0));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.randomthings.magic_hood").withStyle(ChatFormatting.GRAY));
    }
}
