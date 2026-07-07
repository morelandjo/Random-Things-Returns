package lumien.randomthings.blockentity;

import dev.architectury.registry.menu.ExtendedMenuProvider;
import lumien.randomthings.menu.AdvancedRedstoneTorchMenu;
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

public class AdvancedRedstoneTorchBlockEntity extends BlockEntity implements ExtendedMenuProvider {
    private int signalStrengthRed = 15;
    private int signalStrengthGreen = 0;

    public AdvancedRedstoneTorchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ADVANCED_REDSTONE_TORCH.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("signalStrengthRed", signalStrengthRed);
        tag.putInt("signalStrengthGreen", signalStrengthGreen);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.signalStrengthRed = tag.getInt("signalStrengthRed");
        this.signalStrengthGreen = tag.getInt("signalStrengthGreen");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt("signalStrengthRed", signalStrengthRed);
        tag.putInt("signalStrengthGreen", signalStrengthGreen);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public int signalStrengthRed() {
        return signalStrengthRed;
    }

    public int signalStrengthGreen() {
        return signalStrengthGreen;
    }

    public void setSignalStrengthGreen(int newValue) {
        this.signalStrengthGreen = newValue;
        onValueChanged();
    }

    public void setSignalStrengthRed(int newValue) {
        this.signalStrengthRed = newValue;
        onValueChanged();
    }

    private void onValueChanged() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.advanced_redstone_torch");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AdvancedRedstoneTorchMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public void saveExtraData(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }
}
