package lumien.randomthings.blockentity;

import lumien.randomthings.block.OnlineDetectorBlock;
import lumien.randomthings.menu.OnlineDetectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class OnlineDetectorBlockEntity extends BlockEntity implements MenuProvider {
    private String username = "";
    private boolean playerOnline = false;
    
    public OnlineDetectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(null /* ModBlockEntityTypes.ONLINE_DETECTOR.get() */, pos, blockState);
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Username", this.username);
        tag.putBoolean("PlayerOnline", this.playerOnline);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.username = tag.getString("Username");
        this.playerOnline = tag.getBoolean("PlayerOnline");
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("Username", this.username);
        return tag;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.username = tag.getString("Username");
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, OnlineDetectorBlockEntity blockEntity) {
        // Check every second (20 ticks)
        if (level.getGameTime() % 20 == 0) {
            blockEntity.checkPlayerOnline(level, pos);
        }
    }
    
    private void checkPlayerOnline(Level level, BlockPos pos) {
        if (this.username.isEmpty()) {
            setPlayerOnline(false, level, pos);
            return;
        }
        
        // Check if player is online on the server
        boolean isOnline = false;
        if (level.getServer() != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayerByName(this.username);
            isOnline = player != null;
        }
        
        setPlayerOnline(isOnline, level, pos);
    }
    
    private void setPlayerOnline(boolean online, Level level, BlockPos pos) {
        if (this.playerOnline != online) {
            this.playerOnline = online;
            this.setChanged();
            
            // Update block state
            if (level.getBlockState(pos).getBlock() instanceof OnlineDetectorBlock onlineDetectorBlock) {
                onlineDetectorBlock.setPowered(level, pos, online);
            }
        }
    }
    
    // Getters and setters
    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
    
    public boolean isPlayerOnline() {
        return this.playerOnline;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.online_detector");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new OnlineDetectorMenu(containerId, playerInventory, this);
    }
}