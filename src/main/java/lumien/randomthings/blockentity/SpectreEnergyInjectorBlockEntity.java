package lumien.randomthings.blockentity;

import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity for Spectre Energy Injector.
 * Receives energy from external sources and stores it in the owner's Spectre Energy buffer.
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("owner")) {
            this.owner = tag.getUUID("owner");
        }
    }

    /**
     * Get the energy storage capability that receives energy into the player's buffer.
     */
    @Nullable
    public IEnergyStorage getEnergyStorage(Direction side) {
        if (owner == null || !(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        return SpectreCoilHandler.get(serverLevel).getStorage(owner);
    }
}
