package lumien.randomthings.blockentity;

import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Receives energy from external sources into the owner's Spectre Energy buffer. The actual energy
 * capability/API exposure is registered platform-side (Forge Energy on Forge, optional Team Reborn
 * Energy on Fabric); both delegate to {@link #receiveEnergy}.
 */
public class SpectreEnergyInjectorBlockEntity extends BlockEntity {
    private UUID owner;

    public SpectreEnergyInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SPECTRE_ENERGY_INJECTOR.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    /** Push energy into the owner's buffer; returns the amount accepted. */
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        return SpectreCoilHandler.get(serverLevel.getServer()).receive(owner, maxReceive, simulate);
    }

    /** Current buffer level of the owner (for capability getEnergyStored views). */
    public int getStoredEnergy() {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        return SpectreCoilHandler.get(serverLevel.getServer()).getEnergy(owner);
    }

    /** Set the owner's buffer level directly (used by transaction-rollback adapters). */
    public void setStoredEnergy(int energy) {
        if (owner != null && level instanceof ServerLevel serverLevel) {
            SpectreCoilHandler.get(serverLevel.getServer()).setEnergy(owner, energy);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("owner")) {
            this.owner = tag.getUUID("owner");
        }
    }
}
