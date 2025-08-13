package lumien.randomthings.blockentity;

import lumien.randomthings.menu.NotificationInterfaceMenu;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.MessageNotification;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;

public class NotificationInterfaceBlockEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    private String title = "";
    private String description = "";
    private boolean lastRedstoneState = false;
    
    // Item handler for the icon slot (1 slot)
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public NotificationInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.NOTIFICATION_INTERFACE.get(), pos, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("title", title);
        tag.putString("description", description);
        tag.putBoolean("lastRedstoneState", lastRedstoneState);
        
        if (this.owner != null) {
            tag.putString("owner", owner.toString());
        }
        
        tag.put("inventory", itemHandler.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.title = tag.getString("title").trim();
        this.description = tag.getString("description").trim();
        this.lastRedstoneState = tag.getBoolean("lastRedstoneState");
        
        if (tag.contains("owner")) {
            this.owner = UUID.fromString(tag.getString("owner"));
        }
        
        if (tag.contains("inventory")) {
            itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        }
    }

    public void neighborChanged() {
        if (level == null || level.isClientSide) return;
        
        boolean currentRedstoneState = level.hasNeighborSignal(worldPosition);
        
        // Trigger notification on redstone low-to-high transition
        if (!lastRedstoneState && currentRedstoneState) {
            triggerNotification();
        }
        
        lastRedstoneState = currentRedstoneState;
    }

    private void triggerNotification() {
        if (this.owner != null && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(owner);
            
            if (player != null) {
                ItemStack iconStack = itemHandler.getStackInSlot(0);
                MessageNotification message = new MessageNotification(title, description, iconStack);
                RTPacketHandler.sendToPlayer(message, player);
            }
        }
    }

    public void setData(String title, String description) {
        this.title = title != null ? title.trim() : "";
        this.description = description != null ? description.trim() : "";
        this.setChanged();
        
        // Sync to clients
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setPlayerUUID(UUID id) {
        this.owner = id;
        this.setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new NotificationInterfaceMenu(windowId, playerInventory, ContainerLevelAccess.create(this.level, this.worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.randomthings.notification_interface");
    }

    // Client-server sync methods
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            // Initialize redstone state
            lastRedstoneState = level.hasNeighborSignal(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide) {
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), itemHandler.getStackInSlot(i));
            }
        }
    }
}