package lumien.randomthings.item;

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

import java.util.List;

public class ItemSoundPattern extends Item {

    public ItemSoundPattern() {
        super(new Item.Properties()
                .stacksTo(64));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift+right-click to clear the sound
        if (player.isShiftKeyDown()) {
            ResourceLocation sound = getSoundLocation(stack);

            if (sound != null) {
                // Remove the sound data component
                stack.remove(ModDataComponents.SOUND_LOCATION.get());
                return InteractionResultHolder.success(stack);
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation sound = getSoundLocation(stack);

        if (sound != null) {
            // Add the sound name to the item display name
            return super.getName(stack).copy()
                    .append(" <")
                    .append(sound.getPath())
                    .append(">");
        }

        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceLocation sound = getSoundLocation(stack);

        if (sound != null) {
            tooltipComponents.add(Component.literal(sound.toString())
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.literal("<Empty>")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static ResourceLocation getSoundLocation(ItemStack stack) {
        return stack.get(ModDataComponents.SOUND_LOCATION.get());
    }

    public static void setSoundLocation(ItemStack stack, ResourceLocation sound) {
        stack.set(ModDataComponents.SOUND_LOCATION.get(), sound);
    }
}
