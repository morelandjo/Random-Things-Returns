package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Points toward a stored position (bind by crafting with a Position Filter). The needle is a client
 * model predicate ({@code CompassAngles}).
 */
public class GoldenCompassItem extends Item {

    public GoldenCompassItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        BlockPos target = getTarget(stack);
        if (target == null) {
            tooltip.add(Component.translatable("tooltip.randomthings.golden_compass.unbound").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("X: " + target.getX() + " Z: " + target.getZ()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void setTarget(ItemStack compass, int x, int z) {
        RTNbt.setInt(compass, RTDataKeys.COMPASS_TARGET_X, x);
        RTNbt.setInt(compass, RTDataKeys.COMPASS_TARGET_Z, z);
    }

    @Nullable
    public static BlockPos getTarget(ItemStack compass) {
        if (!RTNbt.has(compass, RTDataKeys.COMPASS_TARGET_X)) {
            return null;
        }
        return new BlockPos(RTNbt.getInt(compass, RTDataKeys.COMPASS_TARGET_X), 0, RTNbt.getInt(compass, RTDataKeys.COMPASS_TARGET_Z));
    }
}
