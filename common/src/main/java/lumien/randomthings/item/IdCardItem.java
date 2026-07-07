package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class IdCardItem extends Item {

    private static final String NAME_SUFFIX = "'s ID Card";

    public IdCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && !hasPlayerData(stack)) {
            setPlayerData(stack, player);
            player.sendSystemMessage(Component.translatable("item.randomthings.id_card.bound", player.getName()));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static void setPlayerData(ItemStack stack, Player player) {
        RTNbt.setUUID(stack, RTDataKeys.PLAYER_UUID, player.getUUID());
        stack.setHoverName(Component.literal(player.getName().getString() + NAME_SUFFIX));
    }

    public static boolean hasPlayerData(ItemStack stack) {
        return RTNbt.has(stack, RTDataKeys.PLAYER_UUID);
    }

    @Nullable
    public static UUID getPlayerUUID(ItemStack stack) {
        return RTNbt.getUUID(stack, RTDataKeys.PLAYER_UUID);
    }

    public static String getPlayerName(ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            String fullName = stack.getHoverName().getString();
            if (fullName.endsWith(NAME_SUFFIX)) {
                return fullName.substring(0, fullName.length() - NAME_SUFFIX.length());
            }
        }
        return "Unknown";
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);

        if (hasPlayerData(stack)) {
            tooltipComponents.add(Component.translatable("item.randomthings.id_card.owner", getPlayerName(stack)));
        } else {
            tooltipComponents.add(Component.translatable("item.randomthings.id_card.unbound"));
        }
    }
}
