package lumien.randomthings.handler;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Handler for Super Lubricent Boots functionality.
 * When worn, these boots eliminate friction when the player is moving,
 * similar to walking on Super Lubricent Ice. Sneaking disables the effect.
 *
 * Implementation Note:
 * In 1.12.2, this was done via ASM by returning a slipperiness value of 1.0.
 * In 1.21.1, we directly multiply velocity after the tick to counteract friction.
 *
 * The key insight: Minecraft applies friction during the tick like this:
 *   motion *= blockFriction * 0.91
 * For normal blocks, blockFriction = 0.6, so motion *= 0.546 (major slowdown)
 * For Super Lubricent Stone, blockFriction = 1.099, so motion *= 1.0 (no slowdown)
 *
 * We reverse the normal friction and apply super lubricent friction by multiplying
 * velocity by: superLubricentFriction / blockFriction = 1.099 / 0.6 ≈ 1.832
 *
 * This handler runs on BOTH client and server to ensure smooth movement.
 */
public class SuperLubricentBootsHandler {

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Check if player is wearing Super Lubricent Boots
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty() || boots.getItem() != ModItems.SUPER_LUBRICENT_BOOTS.get()) {
            return;
        }

        // Don't apply effect if sneaking
        if (player.isCrouching()) {
            return;
        }

        // Only apply when on ground (friction only applies when grounded)
        if (!player.onGround()) {
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);

        // Only apply if player has significant horizontal movement
        if (horizontalSpeed > 0.001) {
            // Get the block friction value
            BlockPos blockBelow = player.getBlockPosBelowThatAffectsMyMovement();
            BlockState blockState = player.level().getBlockState(blockBelow);
            float blockFriction = blockState.getBlock().getFriction();

            // Minecraft applies: motion *= blockFriction * 0.91
            // We want Super Lubricent effect: motion *= 1.099 * 0.91 ≈ 1.0
            // So we need to multiply by: 1.099 / blockFriction

            double superLubricentFriction = 1.099; // Same as Super Lubricent Stone
            double correctionFactor = superLubricentFriction / blockFriction;

            // Apply the correction to horizontal velocity only
            player.setDeltaMovement(
                motion.x * correctionFactor,
                motion.y,
                motion.z * correctionFactor
            );
        }
    }
}
