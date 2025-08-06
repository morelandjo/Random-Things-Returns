package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.menu.AdvancedRedstoneRepeaterMenu;
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
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedRedstoneRepeaterBlockEntity extends BlockEntity implements MenuProvider {
    private int turnOnDelay = 20;
    private int turnOffDelay = 20;

    public AdvancedRedstoneRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ADVANCED_REDSTONE_REPEATER.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("turnOnDelay", turnOnDelay);
        tag.putInt("turnOffDelay", turnOffDelay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.turnOnDelay = tag.getInt("turnOnDelay");
        this.turnOffDelay = tag.getInt("turnOffDelay");
    }

    public int getTurnOnDelay() {
        return turnOnDelay;
    }

    public int getTurnOffDelay() {
        return turnOffDelay;
    }

    public void setTurnOnDelay(int delay) {
        this.turnOnDelay = delay;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void setTurnOffDelay(int delay) {
        this.turnOffDelay = delay;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void decreaseTurnOnDelay(int amount) {
        turnOnDelay = Math.max(2, turnOnDelay - amount);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void increaseTurnOnDelay(int amount) {
        turnOnDelay = Math.min(10000, turnOnDelay + amount);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void decreaseTurnOffDelay(int amount) {
        turnOffDelay = Math.max(2, turnOffDelay - amount);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void increaseTurnOffDelay(int amount) {
        turnOffDelay = Math.min(10000, turnOffDelay + amount);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new AdvancedRedstoneRepeaterMenu(windowId, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("randomthings.block.advanced_redstone_repeater");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}