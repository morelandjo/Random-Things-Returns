package lumien.randomthings.event;

import dev.architectury.event.events.common.TickEvent;
import lumien.randomthings.item.LavaCharmItem;
import lumien.randomthings.item.LavaWadersItem;
import lumien.randomthings.item.ObsidianWaterWalkingBootsItem;
import lumien.randomthings.item.SuperLubricentBootsItem;
import lumien.randomthings.item.WaterWalkingBootsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

/**
 * Per-tick behaviour for the boots: water-walking (Water Walking Boots + Lava Waders), lava-walking
 * (Lava Waders), frictionless movement (Super Lubricent Boots), and the Lava Waders recharge tick.
 * Runs on {@link TickEvent#PLAYER_POST} (both sides — position/velocity correction needs the client).
 */
public final class BootsHandler {

    private BootsHandler() {
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(BootsHandler::onPlayerTick);
    }

    private static void onPlayerTick(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        Item item = boots.getItem();
        boolean lavaWaders = item instanceof LavaWadersItem;
        boolean waterWalk = item instanceof WaterWalkingBootsItem || lavaWaders
            || item instanceof ObsidianWaterWalkingBootsItem;

        if (waterWalk && !player.isCrouching()) {
            fluidWalk(player, Fluids.WATER);
        }
        if (lavaWaders && !player.isCrouching() && fluidWalk(player, Fluids.LAVA)) {
            player.clearFire();
        }
        if (item instanceof SuperLubricentBootsItem) {
            applyLubricent(player);
        }
        if (lavaWaders && !player.level().isClientSide) {
            LavaCharmItem.tickCharge(boots);
        }
    }

    /** Snap the player onto a fluid surface they are standing over. Returns true if applied. */
    private static boolean fluidWalk(Player player, Fluid fluid) {
        Level level = player.level();
        BlockPos belowPos = BlockPos.containing(player.getX(), player.getY() - 0.1, player.getZ());
        BlockState belowState = level.getBlockState(belowPos);
        if (belowState.getFluidState().getType() != fluid) {
            return false;
        }
        double surfaceY = belowPos.getY() + 1.0;
        if (player.getY() >= surfaceY + 0.2) {
            return false;
        }
        BlockPos headPos = BlockPos.containing(player.getX(), player.getY() + player.getBbHeight(), player.getZ());
        if (level.getBlockState(headPos).getFluidState().getType() == fluid) {
            return false; // submerged — don't levitate
        }
        player.setPos(player.getX(), surfaceY, player.getZ());
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0) {
            player.setDeltaMovement(motion.x, 0, motion.z);
        }
        player.setOnGround(true);
        player.fallDistance = 0;
        return true;
    }

    /** Counteract ground friction so the player slides as if on Super Lubricent ice. */
    private static void applyLubricent(Player player) {
        if (player.isCrouching() || !player.onGround()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal <= 0.001) {
            return;
        }
        // Equivalent of the protected Entity#getBlockPosBelowThatAffectsMyMovement.
        BlockPos below = BlockPos.containing(player.getX(), player.getBoundingBox().minY - 0.5, player.getZ());
        float blockFriction = player.level().getBlockState(below).getBlock().getFriction();
        double correction = 1.099 / blockFriction;
        player.setDeltaMovement(motion.x * correction, motion.y, motion.z * correction);
    }
}
