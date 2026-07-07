package lumien.randomthings.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Cross-loader energy seam for the Spectre network. The Spectre buffer itself is plain
 * {@code SavedData}; only the two interop points with other mods' energy go through this bridge:
 * coils <em>pushing</em> into adjacent machines (this interface), and the injector <em>exposing</em>
 * a receive-only storage (registered platform-side).
 *
 * <p>Forge implements this with Forge Energy (built into the loader — no added dependency).
 * Fabric implements it with an <em>optional</em> Team Reborn Energy integration: compile-only,
 * activated at runtime only when another mod ships the API. Without it the bridge no-ops, which
 * loses nothing — without TR Energy there are no energy-receiving machines to push into.</p>
 */
public interface IEnergyBridge {

    /**
     * Attempts to insert up to {@code maxAmount} energy into the block at {@code pos}, approaching
     * from {@code side}. Returns the amount that was (or would be, if simulating) accepted.
     */
    int insertEnergy(Level level, BlockPos pos, Direction side, int maxAmount, boolean simulate);

    /** Whether the block at {@code pos} exposes energy storage that can receive from {@code side}. */
    boolean canReceiveEnergy(Level level, BlockPos pos, Direction side);

    /**
     * Charges the energy-storing item in {@code player}'s inventory slot by up to {@code maxAmount}
     * (used by the Spectre Charger). Returns the amount accepted, or 0 if the item stores no energy
     * or no energy API is present. {@code player}/{@code slot} are needed for Fabric's item context.
     */
    int chargeItem(net.minecraft.server.level.ServerPlayer player, int slot, int maxAmount, boolean simulate);
}
