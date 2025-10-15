package lumien.randomthings.blockentity;

import lumien.randomthings.block.SpectreCoilBlock;
import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Block entity for Spectre Coils.
 * Transfers energy from the owner's Spectre Energy buffer to adjacent machines.
 */
public class SpectreCoilBlockEntity extends BlockEntity {
    private UUID owner;
    private SpectreCoilBlock.CoilType coilType = SpectreCoilBlock.CoilType.NORMAL;

    public SpectreCoilBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.SPECTRE_COIL.get(), pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public void setCoilType(SpectreCoilBlock.CoilType type) {
        this.coilType = type;
        setChanged();
    }

    public SpectreCoilBlock.CoilType getCoilType() {
        return coilType;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("coilType", coilType.ordinal());
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.coilType = SpectreCoilBlock.CoilType.values()[tag.getInt("coilType")];
        if (tag.hasUUID("owner")) {
            this.owner = tag.getUUID("owner");
        }
    }

    /**
     * Tick method - transfers energy from player's buffer to adjacent machine.
     */
    public void tick() {
        if (level == null || level.isClientSide || owner == null) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get the facing direction (where the coil connects)
        Direction facing = getBlockState().getValue(SpectreCoilBlock.FACING).getOpposite();
        BlockPos targetPos = worldPosition.relative(facing);

        // Get the target block entity
        BlockEntity targetBe = level.getBlockEntity(targetPos);
        if (targetBe == null) {
            return;
        }

        // Get the energy capability from the target
        IEnergyStorage targetStorage = level.getCapability(
            Capabilities.EnergyStorage.BLOCK,
            targetPos,
            getBlockState(),
            this,
            facing.getOpposite()
        );

        if (targetStorage == null || !targetStorage.canReceive()) {
            return;
        }

        // Handle creative/infinite coils (NUMBER and GENESIS)
        if (coilType == SpectreCoilBlock.CoilType.NUMBER || coilType == SpectreCoilBlock.CoilType.GENESIS) {
            int amount = coilType == SpectreCoilBlock.CoilType.NUMBER ? 1000000 : 10000000; // Configurable or infinite
            targetStorage.receiveEnergy(amount, false);
            return;
        }

        // Get the player's energy storage
        IEnergyStorage coilStorage = SpectreCoilHandler.get(serverLevel).getStorageCoil(owner);

        // Determine transfer rate based on coil type
        int rate = switch (coilType) {
            case NORMAL -> 1024;
            case REDSTONE -> 4096;
            case ENDER -> 20480;
            default -> 1;
        };

        // Try to extract energy from player's buffer
        int available = coilStorage.extractEnergy(rate, true);
        if (available <= 0) {
            return;
        }

        // Try to insert into target
        int accepted = targetStorage.receiveEnergy(available, false);
        if (accepted > 0) {
            // Actually extract the energy that was accepted
            coilStorage.extractEnergy(accepted, false);
        }
    }

    /**
     * Create ticker for server-side energy transfer.
     */
    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker() {
        return (level, pos, state, blockEntity) -> {
            if (blockEntity instanceof SpectreCoilBlockEntity coil) {
                coil.tick();
            }
        };
    }
}
