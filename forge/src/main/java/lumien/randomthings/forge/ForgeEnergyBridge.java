package lumien.randomthings.forge;

import lumien.randomthings.platform.IEnergyBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

/** Energy seam backed by Forge Energy — built into Forge, no additional dependency. */
public class ForgeEnergyBridge implements IEnergyBridge {

    @Override
    public int insertEnergy(Level level, BlockPos pos, Direction side, int maxAmount, boolean simulate) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return 0;
        }
        return be.getCapability(ForgeCapabilities.ENERGY, side)
            .map(storage -> storage.canReceive() ? storage.receiveEnergy(maxAmount, simulate) : 0)
            .orElse(0);
    }

    @Override
    public int chargeItem(net.minecraft.server.level.ServerPlayer player, int slot, int maxAmount, boolean simulate) {
        net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(slot);
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY)
            .map(storage -> storage.canReceive() ? storage.receiveEnergy(maxAmount, simulate) : 0)
            .orElse(0);
    }

    @Override
    public boolean canReceiveEnergy(Level level, BlockPos pos, Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return false;
        }
        return be.getCapability(ForgeCapabilities.ENERGY, side)
            .map(net.minecraftforge.energy.IEnergyStorage::canReceive)
            .orElse(false);
    }
}
