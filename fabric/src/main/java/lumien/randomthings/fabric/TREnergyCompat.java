package lumien.randomthings.fabric;

import lumien.randomthings.blockentity.ModBlockEntityTypes;
import lumien.randomthings.blockentity.SpectreEnergyInjectorBlockEntity;
import lumien.randomthings.handler.spectrecoil.SpectreCoilHandler;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import team.reborn.energy.api.EnergyStorage;

/**
 * The only class that references Team Reborn Energy types. Never classloaded unless
 * {@link FabricEnergyBridge#TR_ENERGY_PRESENT} confirmed the API is available at runtime.
 */
final class TREnergyCompat {

    private TREnergyCompat() {
    }

    static int insert(Level level, BlockPos pos, Direction side, int maxAmount, boolean simulate) {
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, side);
        if (storage == null || !storage.supportsInsertion()) {
            return 0;
        }
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = storage.insert(maxAmount, tx);
            if (!simulate) {
                tx.commit();
            }
            return (int) Math.min(inserted, Integer.MAX_VALUE);
        }
    }

    static boolean canReceive(Level level, BlockPos pos, Direction side) {
        EnergyStorage storage = EnergyStorage.SIDED.find(level, pos, side);
        return storage != null && storage.supportsInsertion();
    }

    static int chargeItem(net.minecraft.server.level.ServerPlayer player, int slot, int maxAmount, boolean simulate) {
        var inventory = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(player.getInventory(), null);
        var slotStorage = inventory.getSlots().get(slot);
        var context = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slotStorage);
        EnergyStorage energy = EnergyStorage.ITEM.find(slotStorage.getResource().toStack(), context);
        if (energy == null || !energy.supportsInsertion()) {
            return 0;
        }
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = energy.insert(maxAmount, tx);
            if (!simulate) {
                tx.commit();
            }
            return (int) Math.min(inserted, Integer.MAX_VALUE);
        }
    }

    static void registerInjector() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (injector, direction) -> new InjectorAdapter(injector),
            ModBlockEntityTypes.SPECTRE_ENERGY_INJECTOR.get());
    }

    /** Receive-only view over the owner's Spectre buffer, with proper transaction rollback. */
    private static final class InjectorAdapter extends SnapshotParticipant<Integer> implements EnergyStorage {
        private final SpectreEnergyInjectorBlockEntity injector;

        InjectorAdapter(SpectreEnergyInjectorBlockEntity injector) {
            this.injector = injector;
        }

        @Override
        protected Integer createSnapshot() {
            return injector.getStoredEnergy();
        }

        @Override
        protected void readSnapshot(Integer snapshot) {
            injector.setStoredEnergy(snapshot);
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            int clamped = (int) Math.min(maxAmount, Integer.MAX_VALUE);
            int accepted = injector.receiveEnergy(clamped, true);
            if (accepted > 0) {
                updateSnapshots(transaction);
                injector.receiveEnergy(accepted, false);
            }
            return accepted;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            return injector.getStoredEnergy();
        }

        @Override
        public long getCapacity() {
            return SpectreCoilHandler.MAX_ENERGY;
        }
    }
}
