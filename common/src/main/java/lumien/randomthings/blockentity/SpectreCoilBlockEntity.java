package lumien.randomthings.blockentity;

import lumien.randomthings.block.SpectreCoilBlock;
import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import lumien.randomthings.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Transfers energy from the owner's Spectre Energy buffer into the machine the coil is attached to.
 * The push goes through {@code Services.ENERGY} — the cross-loader energy seam.
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("coilType", coilType.ordinal());
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        int ordinal = tag.getInt("coilType");
        this.coilType = SpectreCoilBlock.CoilType.values()[Math.floorMod(ordinal, SpectreCoilBlock.CoilType.values().length)];
        if (tag.hasUUID("owner")) {
            this.owner = tag.getUUID("owner");
        }
    }

    public void tick() {
        if (level == null || level.isClientSide || owner == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Direction facing = getBlockState().getValue(SpectreCoilBlock.FACING).getOpposite();
        BlockPos targetPos = worldPosition.relative(facing);
        Direction insertSide = facing.getOpposite();

        // Creative coils: push their rate unconditionally.
        if (coilType == SpectreCoilBlock.CoilType.NUMBER || coilType == SpectreCoilBlock.CoilType.GENESIS) {
            Services.ENERGY.insertEnergy(level, targetPos, insertSide, coilType.getTransferRate(), false);
            return;
        }

        SpectreCoilHandler handler = SpectreCoilHandler.get(serverLevel.getServer());
        int available = handler.extract(owner, coilType.getTransferRate(), true);
        if (available <= 0) {
            return;
        }
        int accepted = Services.ENERGY.insertEnergy(level, targetPos, insertSide, available, false);
        if (accepted > 0) {
            handler.extract(owner, accepted, false);
        }
    }

    public static <T extends BlockEntity> BlockEntityTicker<T> createTicker() {
        return (level, pos, state, blockEntity) -> {
            if (blockEntity instanceof SpectreCoilBlockEntity coil) {
                coil.tick();
            }
        };
    }
}
