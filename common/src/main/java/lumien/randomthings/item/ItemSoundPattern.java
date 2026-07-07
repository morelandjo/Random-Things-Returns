package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSoundPattern extends Item {

    public ItemSoundPattern() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift+right-click clears the stored sound.
        if (player.isShiftKeyDown() && getSoundLocation(stack) != null) {
            RTNbt.remove(stack, RTDataKeys.SOUND_LOCATION);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation sound = getSoundLocation(stack);
        if (sound != null) {
            return super.getName(stack).copy().append(" <").append(sound.getPath()).append(">");
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation sound = getSoundLocation(stack);
        if (sound != null) {
            tooltipComponents.add(Component.literal(sound.toString()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.literal("<Empty>").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Nullable
    public static ResourceLocation getSoundLocation(ItemStack stack) {
        return RTNbt.getResourceLocation(stack, RTDataKeys.SOUND_LOCATION);
    }

    public static void setSoundLocation(ItemStack stack, ResourceLocation sound) {
        RTNbt.setResourceLocation(stack, RTDataKeys.SOUND_LOCATION, sound);
    }
}
