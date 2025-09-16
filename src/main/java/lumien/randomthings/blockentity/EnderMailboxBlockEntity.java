package lumien.randomthings.blockentity;

import lumien.randomthings.block.EnderMailboxBlock;
import lumien.randomthings.handler.EnderLetterHandler;
import lumien.randomthings.item.EnderLetterItem;
import lumien.randomthings.item.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class EnderMailboxBlockEntity extends BlockEntity implements MenuProvider {
    private static final int INVENTORY_SIZE = 27;
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE);
    private UUID ownerUUID;
    private String ownerName = "";
    private int activeTimer = 0;

    public EnderMailboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ENDER_MAILBOX.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnderMailboxBlockEntity mailbox) {
        // Handle active state timing
        if (mailbox.activeTimer > 0) {
            mailbox.activeTimer--;
            if (mailbox.activeTimer == 0) {
                EnderMailboxBlock.setActive(level, pos, false);
            }
        }

        // Check for incoming mail
        if (mailbox.ownerUUID != null) {
            ItemStack incomingLetter = EnderLetterHandler.getIncomingLetter(mailbox.ownerUUID);
            if (!incomingLetter.isEmpty()) {
                if (mailbox.addLetter(incomingLetter)) {
                    EnderLetterHandler.removeIncomingLetter(mailbox.ownerUUID);
                    mailbox.setActive();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        if (ownerUUID != null) {
            tag.putUUID("ownerUUID", ownerUUID);
        }
        tag.putString("ownerName", ownerName);
        tag.putInt("activeTimer", activeTimer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        if (tag.hasUUID("ownerUUID")) {
            ownerUUID = tag.getUUID("ownerUUID");
        }
        ownerName = tag.getString("ownerName");
        activeTimer = tag.getInt("activeTimer");
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean isOwner(Player player) {
        if (ownerUUID == null) {
            setOwner(player);
            return true;
        }
        return ownerUUID.equals(player.getUUID());
    }

    public boolean sendLetter(ItemStack letterStack, Player sender) {
        if (!(letterStack.getItem() instanceof EnderLetterItem)) {
            return false;
        }

        String receiverName = EnderLetterItem.getReceiver(letterStack);
        if (receiverName == null || receiverName.isEmpty()) {
            sender.displayClientMessage(Component.translatable("item.randomthings.ender_letter.no_player", ""), true);
            return false;
        }

        // Set sender information
        EnderLetterItem.setSender(letterStack, sender.getName().getString());
        letterStack.set(ModDataComponents.PLAYER_UUID.get(), sender.getUUID());
        EnderLetterItem.setSigned(letterStack, true);

        // Try to send the letter
        boolean success = EnderLetterHandler.sendLetter(letterStack, receiverName);
        if (!success) {
            sender.displayClientMessage(Component.translatable("item.randomthings.ender_letter.no_space"), true);
        } else {
            setActive();
        }

        return success;
    }

    private boolean addLetter(ItemStack letterStack) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) {
                inventory.setStackInSlot(i, letterStack);
                setChanged();
                return true;
            }
        }
        return false;
    }

    private void setActive() {
        this.activeTimer = 60; // 3 seconds at 20 ticks per second
        if (level != null) {
            EnderMailboxBlock.setActive(level, worldPosition, true);
        }
        setChanged();
    }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.randomthings.ender_mailbox");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new lumien.randomthings.menu.EnderMailboxMenu(id, playerInventory, worldPosition);
    }
}