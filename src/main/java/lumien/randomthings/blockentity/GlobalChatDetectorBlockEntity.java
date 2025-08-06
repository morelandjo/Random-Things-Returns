package lumien.randomthings.blockentity;

import lumien.randomthings.block.GlobalChatDetectorBlock;
import lumien.randomthings.event.ChatEventHandler;
import lumien.randomthings.item.IdCardItem;
import lumien.randomthings.menu.GlobalChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;

import javax.annotation.Nullable;
import java.util.UUID;

public class GlobalChatDetectorBlockEntity extends BlockEntity implements MenuProvider, Container {
    private String detectionMessage = "";
    private boolean consumeMessage = false;
    private UUID ownerUUID;
    private int powerDuration = 0;
    private static final int POWER_DURATION_TICKS = 20; // 1 second
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY); // 9 slots for ID Cards
    
    public GlobalChatDetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.GLOBAL_CHAT_DETECTOR.get(), pos, blockState);
        ChatEventHandler.registerGlobalChatDetector(this);
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        ChatEventHandler.unregisterGlobalChatDetector(this);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("DetectionMessage", this.detectionMessage);
        tag.putBoolean("ConsumeMessage", this.consumeMessage);
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putInt("PowerDuration", this.powerDuration);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.detectionMessage = tag.getString("DetectionMessage");
        this.consumeMessage = tag.getBoolean("ConsumeMessage");
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        this.powerDuration = tag.getInt("PowerDuration");
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("DetectionMessage", this.detectionMessage);
        tag.putBoolean("ConsumeMessage", this.consumeMessage);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.detectionMessage = tag.getString("DetectionMessage");
        this.consumeMessage = tag.getBoolean("ConsumeMessage");
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, GlobalChatDetectorBlockEntity blockEntity) {
        if (blockEntity.powerDuration > 0) {
            blockEntity.powerDuration--;
            if (blockEntity.powerDuration == 0) {
                if (level.getBlockState(pos).getBlock() instanceof GlobalChatDetectorBlock globalChatDetectorBlock) {
                    globalChatDetectorBlock.setPowered(level, pos, false);
                }
            }
        }
    }
    
    public void onChatMessage(String message, UUID playerUUID) {
        if (matchesDetectionMessage(message)) {
            trigger();
        }
    }
    
    private boolean matchesDetectionMessage(String message) {
        if (this.detectionMessage.isEmpty()) {
            return false;
        }
        return message.toLowerCase().contains(this.detectionMessage.toLowerCase());
    }
    
    private void trigger() {
        if (this.level != null && !this.level.isClientSide) {
            this.powerDuration = POWER_DURATION_TICKS;
            if (this.level.getBlockState(this.worldPosition).getBlock() instanceof GlobalChatDetectorBlock globalChatDetectorBlock) {
                globalChatDetectorBlock.setPowered(this.level, this.worldPosition, true);
            }
        }
    }
    
    // Getters and setters
    public String getDetectionMessage() {
        return this.detectionMessage;
    }
    
    public void setDetectionMessage(String message) {
        this.detectionMessage = message;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
    
    public boolean isConsumeMessage() {
        return this.consumeMessage;
    }
    
    public void setConsumeMessage(boolean consume) {
        this.consumeMessage = consume;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
    
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
    
    public void setOwnerUUID(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
        this.setChanged();
    }
    
    public boolean shouldConsumeMessage(String message, UUID playerUUID) {
        if (!this.consumeMessage || !matchesDetectionMessage(message)) {
            return false;
        }
        
        // Check if we have an ID card for this player in any of the 9 slots
        for (ItemStack idCard : this.items) {
            if (idCard.getItem() instanceof IdCardItem && IdCardItem.hasPlayerData(idCard)) {
                UUID cardPlayerUUID = IdCardItem.getPlayerUUID(idCard);
                if (playerUUID.equals(cardPlayerUUID)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Container implementation
    @Override
    public int getContainerSize() {
        return this.items.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }
    
    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }
    
    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }
    
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
    
    @Override
    public void clearContent() {
        this.items.clear();
    }
    
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= 0 && slot < 9 && stack.getItem() instanceof IdCardItem;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.global_chat_detector");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GlobalChatDetectorMenu(containerId, playerInventory, this);
    }
}