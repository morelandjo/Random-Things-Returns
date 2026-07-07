package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.menu.AdvancedRedstoneRepeaterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedRedstoneRepeaterBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private int turnOnDelay = 20;
    private int turnOffDelay = 20;

    public AdvancedRedstoneRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ADVANCED_REDSTONE_REPEATER.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("turnOnDelay", turnOnDelay);
        tag.putInt("turnOffDelay", turnOffDelay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.turnOnDelay = tag.getInt("turnOnDelay");
        this.turnOffDelay = tag.getInt("turnOffDelay");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("turnOnDelay", turnOnDelay);
        tag.putInt("turnOffDelay", turnOffDelay);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int getTurnOnDelay() {
        return turnOnDelay;
    }

    public int getTurnOffDelay() {
        return turnOffDelay;
    }

    public void setTurnOnDelay(int delay) {
        this.turnOnDelay = delay;
        sync();
    }

    public void setTurnOffDelay(int delay) {
        this.turnOffDelay = delay;
        sync();
    }

    public void decreaseTurnOnDelay(int amount) {
        this.turnOnDelay = Math.max(2, turnOnDelay - amount);
        sync();
    }

    public void increaseTurnOnDelay(int amount) {
        this.turnOnDelay = Math.min(10000, turnOnDelay + amount);
        sync();
    }

    public void decreaseTurnOffDelay(int amount) {
        this.turnOffDelay = Math.max(2, turnOffDelay - amount);
        sync();
    }

    public void increaseTurnOffDelay(int amount) {
        this.turnOffDelay = Math.min(10000, turnOffDelay + amount);
        sync();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new AdvancedRedstoneRepeaterMenu(windowId, playerInventory, this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.advanced_redstone_repeater");
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
