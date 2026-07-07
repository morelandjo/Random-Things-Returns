package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.BasicRedstoneInterfaceBlockEntity;
import lumien.randomthings.blockentity.RedstoneObserverBlockEntity;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneToolItem extends Item {

    public RedstoneToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockState clickedState = level.getBlockState(clickedPos);
        Block clickedBlock = clickedState.getBlock();
        boolean isLinkable = isLinkableBlock(clickedBlock);

        BlockPos linkFrom = RTNbt.getBlockPos(stack, RTDataKeys.REDSTONE_TOOL_LINK);

        if (linkFrom == null) {
            // Not linking — only accept first click on a linkable block.
            if (!isLinkable) {
                return InteractionResult.FAIL;
            }
            RTNbt.setBlockPos(stack, RTDataKeys.REDSTONE_TOOL_LINK, clickedPos.immutable());
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.translatable("message.randomthings.redstone_tool.linking_started",
                        clickedPos.getX(), clickedPos.getY(), clickedPos.getZ())
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Already in linking mode — the second click sets the target on the linker.
        // If the player clicks the source again, cancel.
        if (linkFrom.equals(clickedPos)) {
            RTNbt.remove(stack, RTDataKeys.REDSTONE_TOOL_LINK);
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.translatable("message.randomthings.redstone_tool.linking_cancelled")
                        .withStyle(ChatFormatting.YELLOW), true);
            }
            return InteractionResult.SUCCESS;
        }

        BlockEntity linkerBe = level.getBlockEntity(linkFrom);
        boolean linked = false;
        if (linkerBe instanceof RedstoneObserverBlockEntity observer) {
            observer.setTarget(clickedPos.immutable());
            linked = true;
        } else if (linkerBe instanceof BasicRedstoneInterfaceBlockEntity iface) {
            iface.setTarget(clickedPos.immutable());
            linked = true;
        }

        if (linked) {
            RTNbt.remove(stack, RTDataKeys.REDSTONE_TOOL_LINK);
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(
                    Component.translatable("message.randomthings.redstone_tool.linked",
                        clickedPos.getX(), clickedPos.getY(), clickedPos.getZ())
                        .withStyle(ChatFormatting.GREEN), true);
            }
            return InteractionResult.SUCCESS;
        }

        // Linker block no longer exists / isn't valid — drop the link.
        RTNbt.remove(stack, RTDataKeys.REDSTONE_TOOL_LINK);
        return InteractionResult.FAIL;
    }

    private static boolean isLinkableBlock(Block block) {
        return block == ModBlocks.REDSTONE_OBSERVER.get()
            || block == ModBlocks.BASIC_REDSTONE_INTERFACE.get();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return RTNbt.has(stack, RTDataKeys.REDSTONE_TOOL_LINK);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        BlockPos link = RTNbt.getBlockPos(stack, RTDataKeys.REDSTONE_TOOL_LINK);
        if (link != null) {
            tooltip.add(Component.translatable("tooltip.randomthings.redstone_tool.linking",
                link.getX(), link.getY(), link.getZ()).withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.randomthings.redstone_tool").withStyle(ChatFormatting.GRAY));
        }
    }
}
