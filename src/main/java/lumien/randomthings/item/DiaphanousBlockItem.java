package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class DiaphanousBlockItem extends BlockItem {
    
    public DiaphanousBlockItem(Block block, Properties properties) {
        super(block, properties);
    }
    
    
    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);
        
        Boolean inverted = stack.get(ModDataComponents.DIAPHANOUS_INVERTED.get());
        if (inverted != null && inverted) {
            return Component.translatable("block.randomthings.diaphanous_block_inverted");
        }
        
        return baseName;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        // Add information about what block it mimics
        ResourceLocation blockId = stack.get(ModDataComponents.DIAPHANOUS_BLOCK_STATE.get());
        if (blockId != null) {
            try {
                Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(blockId);
                
                if (block != null && block != Blocks.AIR) {
                    ItemStack blockStack = new ItemStack(block);
                    Component blockName = blockStack.getHoverName();
                    tooltip.add(Component.literal("Mimics: ").withStyle(ChatFormatting.GRAY)
                            .append(blockName.copy().withStyle(ChatFormatting.WHITE)));
                }
            } catch (Exception e) {
                tooltip.add(Component.literal("Mimics: Stone").withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.literal("Mimics: Stone").withStyle(ChatFormatting.GRAY));
        }
        
        // Add tooltip about functionality
        tooltip.add(Component.translatable("tooltip.randomthings.diaphanous_block").withStyle(ChatFormatting.DARK_GRAY));
    }
}