package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

/** BlockItem carrying the disguise block id + inverted flag for the Diaphanous Block. */
public class DiaphanousBlockItem extends BlockItem {

    public DiaphanousBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ResourceLocation blockId = RTNbt.getResourceLocation(stack, RTDataKeys.DIAPHANOUS_BLOCK_STATE);
        if (blockId != null) {
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            tooltip.add(block.getName().withStyle(ChatFormatting.GRAY));
        }
        if (RTNbt.getBoolean(stack, RTDataKeys.DIAPHANOUS_INVERTED)) {
            tooltip.add(Component.translatable("tooltip.randomthings.diaphanous_block.inverted").withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.translatable("tooltip.randomthings.diaphanous_block").withStyle(ChatFormatting.DARK_GRAY));
    }
}
