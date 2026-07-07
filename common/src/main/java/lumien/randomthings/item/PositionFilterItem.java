package lumien.randomthings.item;

import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Stores a clicked position (and dimension); used as a target filter. Shift to view the stored spot. */
public class PositionFilterItem extends Item {
    public PositionFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            setPosition(stack, context.getLevel().dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            if (RTNbt.has(stack, RTDataKeys.POSITION_X)) {
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.dimension", getDimension(stack)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.x", RTNbt.getInt(stack, RTDataKeys.POSITION_X)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.y", RTNbt.getInt(stack, RTDataKeys.POSITION_Y)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.z", RTNbt.getInt(stack, RTDataKeys.POSITION_Z)).withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.randomthings.position_filter.shift").withStyle(ChatFormatting.DARK_GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public static void setPosition(ItemStack stack, String dimension, int x, int y, int z) {
        RTNbt.setString(stack, RTDataKeys.POSITION_DIMENSION, dimension);
        RTNbt.setInt(stack, RTDataKeys.POSITION_X, x);
        RTNbt.setInt(stack, RTDataKeys.POSITION_Y, y);
        RTNbt.setInt(stack, RTDataKeys.POSITION_Z, z);
    }

    public static String getDimension(ItemStack stack) {
        return RTNbt.getString(stack, RTDataKeys.POSITION_DIMENSION);
    }

    @Nullable
    public static BlockPos getPosition(ItemStack stack) {
        if (!RTNbt.has(stack, RTDataKeys.POSITION_X)) {
            return null;
        }
        return new BlockPos(RTNbt.getInt(stack, RTDataKeys.POSITION_X), RTNbt.getInt(stack, RTDataKeys.POSITION_Y), RTNbt.getInt(stack, RTDataKeys.POSITION_Z));
    }
}
