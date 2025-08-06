package lumien.randomthings.blockentity;

import lumien.randomthings.block.ChatDetectorBlock;
import lumien.randomthings.event.ChatEventHandler;
import lumien.randomthings.menu.ChatDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class ChatDetectorBlockEntity extends BlockEntity implements MenuProvider {
    private String detectionMessage = "";
    private boolean consumeMessage = false;
    private UUID ownerUUID;
    private int powerDuration = 0;
    private static final int POWER_DURATION_TICKS = 20; // 1 second
    
    public ChatDetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CHAT_DETECTOR.get(), pos, blockState);
        ChatEventHandler.registerChatDetector(this);
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();
        ChatEventHandler.unregisterChatDetector(this);
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
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, ChatDetectorBlockEntity blockEntity) {
        if (blockEntity.powerDuration > 0) {
            blockEntity.powerDuration--;
            if (blockEntity.powerDuration == 0) {
                if (level.getBlockState(pos).getBlock() instanceof ChatDetectorBlock chatDetectorBlock) {
                    chatDetectorBlock.setPowered(level, pos, false);
                }
            }
        }
    }
    
    public void onChatMessage(String message, UUID playerUUID) {
        if (this.ownerUUID != null && !this.ownerUUID.equals(playerUUID)) {
            return; // Only respond to owner's messages
        }
        
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
            if (this.level.getBlockState(this.worldPosition).getBlock() instanceof ChatDetectorBlock chatDetectorBlock) {
                chatDetectorBlock.setPowered(this.level, this.worldPosition, true);
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
        return this.consumeMessage && 
               this.ownerUUID != null && 
               this.ownerUUID.equals(playerUUID) && 
               matchesDetectionMessage(message);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.chat_detector");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChatDetectorMenu(containerId, playerInventory, this);
    }
}