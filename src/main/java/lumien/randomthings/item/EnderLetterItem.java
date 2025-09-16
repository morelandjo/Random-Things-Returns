package lumien.randomthings.item;

import lumien.randomthings.block.EnderMailboxBlock;
import lumien.randomthings.blockentity.EnderMailboxBlockEntity;
import lumien.randomthings.menu.EnderLetterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class EnderLetterItem extends Item {
    public static final int INVENTORY_SIZE = 9;

    public EnderLetterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Don't open GUI if shift-clicking (let block handle letter sending)
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (!level.isClientSide()) {
            // Open the Ender Letter GUI
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inventory, p) -> new EnderLetterMenu(id, inventory, itemStack),
                Component.translatable("container.randomthings.ender_letter")
            ));
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if (player != null && player.isShiftKeyDown() && level.getBlockState(pos).getBlock() instanceof EnderMailboxBlock) {

            if (!level.isClientSide()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof EnderMailboxBlockEntity mailbox) {
                    // Check if player owns this mailbox
                    if (!mailbox.isOwner(player)) {
                        player.displayClientMessage(Component.translatable("block.randomthings.ender_mailbox.owner"), true);
                        return InteractionResult.FAIL;
                    }

                    boolean success = mailbox.sendLetter(itemStack, player);

                    if (success) {
                        if (!player.isCreative()) {
                            itemStack.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Show sender information
        String senderName = stack.get(ModDataComponents.SENDER_NAME.get());
        if (senderName != null && !senderName.isEmpty()) {
            tooltip.add(Component.translatable("item.randomthings.ender_letter.sender", senderName));
        }

        // Show receiver information
        String receiverName = stack.get(ModDataComponents.RECEIVER_NAME.get());
        if (receiverName != null && !receiverName.isEmpty()) {
            tooltip.add(Component.translatable("item.randomthings.ender_letter.receiver", receiverName));
        }

        // Add tooltip description
        tooltip.add(Component.translatable("tooltip.randomthings.ender_letter"));
    }

    public static ItemStackHandler getInventory(ItemStack letterStack) {
        // Create a temporary handler to work with the letter's inventory
        ItemStackHandler handler = new ItemStackHandler(INVENTORY_SIZE);

        // TODO: Load inventory from data components when NBT system is implemented
        // For now, return empty handler

        return handler;
    }

    public static void setInventory(ItemStack letterStack, ItemStackHandler inventory) {
        // TODO: Save inventory to data components when NBT system is implemented
    }

    public static String getSender(ItemStack letterStack) {
        return letterStack.get(ModDataComponents.SENDER_NAME.get());
    }

    public static void setSender(ItemStack letterStack, String senderName) {
        letterStack.set(ModDataComponents.SENDER_NAME.get(), senderName);
    }

    public static String getReceiver(ItemStack letterStack) {
        return letterStack.get(ModDataComponents.RECEIVER_NAME.get());
    }

    public static void setReceiver(ItemStack letterStack, String receiverName) {
        letterStack.set(ModDataComponents.RECEIVER_NAME.get(), receiverName);
    }

    public static boolean isSigned(ItemStack letterStack) {
        Boolean signed = letterStack.get(ModDataComponents.ENDER_LETTER_SIGNED.get());
        return signed != null && signed;
    }

    public static void setSigned(ItemStack letterStack, boolean signed) {
        letterStack.set(ModDataComponents.ENDER_LETTER_SIGNED.get(), signed);
    }
}