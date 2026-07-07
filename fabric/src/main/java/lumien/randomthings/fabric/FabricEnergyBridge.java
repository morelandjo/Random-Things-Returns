package lumien.randomthings.fabric;

import lumien.randomthings.platform.IEnergyBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Energy seam on Fabric via an <em>optional</em> Team Reborn Energy integration. TR Energy is a
 * compile-only dependency: nothing is bundled and nothing is required at runtime. When another mod
 * ships the API (every Fabric energy mod bundles it jar-in-jar), the bridge activates; otherwise it
 * no-ops — and with no TR Energy present there are no energy machines to interact with anyway.
 *
 * <p>All TR classes live in {@link TREnergyCompat}, which is only classloaded behind
 * {@link #TR_ENERGY_PRESENT} so their absence can never throw.</p>
 */
public class FabricEnergyBridge implements IEnergyBridge {

    public static final boolean TR_ENERGY_PRESENT = detectTREnergy();

    private static boolean detectTREnergy() {
        try {
            Class.forName("team.reborn.energy.api.EnergyStorage", false, FabricEnergyBridge.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Called from mod init: registers the injector's energy storage when TR Energy is present. */
    public static void init() {
        if (TR_ENERGY_PRESENT) {
            TREnergyCompat.registerInjector();
        }
    }

    @Override
    public int insertEnergy(Level level, BlockPos pos, Direction side, int maxAmount, boolean simulate) {
        return TR_ENERGY_PRESENT ? TREnergyCompat.insert(level, pos, side, maxAmount, simulate) : 0;
    }

    @Override
    public boolean canReceiveEnergy(Level level, BlockPos pos, Direction side) {
        return TR_ENERGY_PRESENT && TREnergyCompat.canReceive(level, pos, side);
    }

    @Override
    public int chargeItem(net.minecraft.server.level.ServerPlayer player, int slot, int maxAmount, boolean simulate) {
        return TR_ENERGY_PRESENT ? TREnergyCompat.chargeItem(player, slot, maxAmount, simulate) : 0;
    }
}
