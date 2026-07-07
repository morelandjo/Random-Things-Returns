package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Points toward a bound player (bind by crafting with an ID Card). The server refreshes the target
 * coordinates once a second; the needle itself is a client model predicate ({@code CompassAngles}).
 */
public class EmeraldCompassItem extends Item {

    public EmeraldCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && level.getGameTime() % 20 == 0 && level.getServer() != null) {
            UUID playerUUID = getTargetUUID(stack);
            if (playerUUID != null) {
                ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(playerUUID);
                if (targetPlayer != null) {
                    BlockPos targetPos = targetPlayer.blockPosition();
                    RTNbt.setInt(stack, RTDataKeys.COMPASS_TARGET_X, targetPos.getX());
                    RTNbt.setInt(stack, RTDataKeys.COMPASS_TARGET_Z, targetPos.getZ());
                } else {
                    RTNbt.remove(stack, RTDataKeys.COMPASS_TARGET_X);
                    RTNbt.remove(stack, RTDataKeys.COMPASS_TARGET_Z);
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (getTargetUUID(stack) == null) {
            tooltip.add(Component.translatable("tooltip.randomthings.emerald_compass.unbound").withStyle(ChatFormatting.GRAY));
        }
    }

    @Nullable
    public static BlockPos getTarget(ItemStack stack) {
        if (!RTNbt.has(stack, RTDataKeys.COMPASS_TARGET_X)) {
            return null;
        }
        return new BlockPos(RTNbt.getInt(stack, RTDataKeys.COMPASS_TARGET_X), 0, RTNbt.getInt(stack, RTDataKeys.COMPASS_TARGET_Z));
    }

    public static void setTarget(ItemStack compass, UUID playerUUID) {
        RTNbt.setUUID(compass, RTDataKeys.PLAYER_UUID, playerUUID);
    }

    @Nullable
    public static UUID getTargetUUID(ItemStack compass) {
        return RTNbt.getUUID(compass, RTDataKeys.PLAYER_UUID);
    }
}
