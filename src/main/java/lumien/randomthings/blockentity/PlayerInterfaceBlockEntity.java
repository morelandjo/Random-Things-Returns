package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerInterfaceBlockEntity extends BlockEntity {
    private UUID playerUUID;

    public PlayerInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.PLAYER_INTERFACE.get(), pos, blockState);
    }

    public boolean isCurrentlyConnected() {
        return getPlayerInventory() != null;
    }

    @Nullable
    protected Inventory getPlayerInventory() {
        if (this.playerUUID == null || level == null || level.isClientSide) {
            return null;
        }
        
        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerUUID);
        return player != null ? player.getInventory() : null;
    }

    public IItemHandler getItemHandler(@Nullable Direction facing) {
        if (level == null || level.isClientSide) {
            return EMPTY_HANDLER;
        }

        Inventory playerInventory = getPlayerInventory();
        if (playerInventory == null) {
            return EMPTY_HANDLER;
        }

        InvWrapper wrapper = new InvWrapper(playerInventory);

        // Different faces provide access to different inventory sections
        if (facing == Direction.UP) {
            // Armor slots (36-39)
            return new RangedWrapper(wrapper, 36, 40);
        } else if (facing == Direction.DOWN) {
            // Hotbar (0-8)
            return new RangedWrapper(wrapper, 0, 9);
        } else if (facing == Direction.NORTH) {
            // Offhand slot (40)
            return new RangedWrapper(wrapper, 40, 41);
        } else {
            // Main inventory (9-35)
            return new RangedWrapper(wrapper, 9, 36);
        }
    }

    // Simple empty handler implementation
    private static final IItemHandler EMPTY_HANDLER = new IItemHandler() {
        @Override
        public int getSlots() {
            return 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }
    };

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
        this.setChanged();
        
        // Invalidate capabilities when player changes
        if (level != null && !level.isClientSide) {
            level.invalidateCapabilities(worldPosition);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.playerUUID != null) {
            tag.putUUID("player_uuid", this.playerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("player_uuid")) {
            this.playerUUID = tag.getUUID("player_uuid");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        if (this.playerUUID != null) {
            tag.putUUID("player_uuid", this.playerUUID);
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        if (tag.hasUUID("player_uuid")) {
            this.playerUUID = tag.getUUID("player_uuid");
        }
    }
}