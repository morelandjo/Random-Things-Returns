package lumien.randomthings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class PositionFilterItem extends Item {

    public PositionFilterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide()) {
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();

            // Store the position in data components
            setPosition(stack, level.dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Check if shift is held (using GLFW key state check)
        boolean shiftHeld = GLFW.glfwGetKey(
            net.minecraft.client.Minecraft.getInstance().getWindow().getWindow(),
            GLFW.GLFW_KEY_LEFT_SHIFT
        ) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(
            net.minecraft.client.Minecraft.getInstance().getWindow().getWindow(),
            GLFW.GLFW_KEY_RIGHT_SHIFT
        ) == GLFW.GLFW_PRESS;

        if (shiftHeld) {
            String dimension = stack.get(ModDataComponents.POSITION_DIMENSION.get());
            Integer x = stack.get(ModDataComponents.POSITION_X.get());
            Integer y = stack.get(ModDataComponents.POSITION_Y.get());
            Integer z = stack.get(ModDataComponents.POSITION_Z.get());

            if (dimension != null && x != null && y != null && z != null) {
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.dimension", dimension)
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.x", x)
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.y", y)
                    .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.randomthings.position_filter.z", z)
                    .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.randomthings.position_filter.shift")
                .withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    // Static helper methods
    public static void setPosition(ItemStack stack, String dimension, int x, int y, int z) {
        stack.set(ModDataComponents.POSITION_DIMENSION.get(), dimension);
        stack.set(ModDataComponents.POSITION_X.get(), x);
        stack.set(ModDataComponents.POSITION_Y.get(), y);
        stack.set(ModDataComponents.POSITION_Z.get(), z);
    }

    public static String getDimension(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.POSITION_DIMENSION.get(), "");
    }

    public static BlockPos getPosition(ItemStack stack) {
        Integer x = stack.get(ModDataComponents.POSITION_X.get());
        Integer y = stack.get(ModDataComponents.POSITION_Y.get());
        Integer z = stack.get(ModDataComponents.POSITION_Z.get());

        if (x == null || y == null || z == null) {
            return null;
        }

        return new BlockPos(x, y, z);
    }

    public static boolean hasPosition(ItemStack stack) {
        return stack.has(ModDataComponents.POSITION_DIMENSION.get()) &&
               stack.has(ModDataComponents.POSITION_X.get()) &&
               stack.has(ModDataComponents.POSITION_Y.get()) &&
               stack.has(ModDataComponents.POSITION_Z.get());
    }
}
