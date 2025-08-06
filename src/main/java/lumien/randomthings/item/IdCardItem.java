package lumien.randomthings.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class IdCardItem extends Item {
    public IdCardItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            if (!hasPlayerData(stack)) {
                setPlayerData(stack, player);
                player.sendSystemMessage(Component.translatable("item.randomthings.id_card.bound", player.getName()));
            }
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    public static void setPlayerData(ItemStack stack, Player player) {
        stack.set(ModDataComponents.PLAYER_UUID.get(), player.getUUID());
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(player.getName().getString() + "'s ID Card"));
    }
    
    public static boolean hasPlayerData(ItemStack stack) {
        return stack.has(ModDataComponents.PLAYER_UUID.get());
    }
    
    public static UUID getPlayerUUID(ItemStack stack) {
        return stack.get(ModDataComponents.PLAYER_UUID.get());
    }
    
    public static String getPlayerName(ItemStack stack) {
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name != null) {
            String fullName = name.getString();
            if (fullName.endsWith("'s ID Card")) {
                return fullName.substring(0, fullName.length() - "'s ID Card".length());
            }
        }
        return "Unknown";
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        if (hasPlayerData(stack)) {
            String playerName = getPlayerName(stack);
            tooltipComponents.add(Component.translatable("item.randomthings.id_card.owner", playerName));
        } else {
            tooltipComponents.add(Component.translatable("item.randomthings.id_card.unbound"));
        }
    }
}