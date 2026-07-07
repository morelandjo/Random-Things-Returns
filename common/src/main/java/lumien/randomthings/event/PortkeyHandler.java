package lumien.randomthings.event;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PortkeyItem;
import lumien.randomthings.util.PortkeyTarget;
import lumien.randomthings.util.RTDataKeys;
import lumien.randomthings.util.RTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Portkey behaviour invoked from {@code ItemEntityMixin}: age the dropped item, and teleport the
 * player who picks up a primed portkey. (Replaces the 1.21.1 {@code onEntityItemUpdate} +
 * {@code ItemEntityPickupEvent}, which have no cross-loader equivalent.)
 */
public final class PortkeyHandler {

    private static final int PRIME_TICKS = 100;

    private PortkeyHandler() {
    }

    /** Age a dropped portkey (called from ItemEntity#tick). */
    public static void age(ItemEntity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        ItemStack stack = entity.getItem();
        if (stack.getItem() != ModItems.PORTKEY.get()) {
            return;
        }
        int age = RTNbt.getInt(stack, RTDataKeys.PORTKEY_AGE, 0);
        if (age == 0) {
            entity.setUnlimitedLifetime();
        }
        RTNbt.setInt(stack, RTDataKeys.PORTKEY_AGE, age + 1);
    }

    /** Attempt the pickup-teleport. Returns true if handled (the pickup should then be cancelled). */
    public static boolean tryTeleport(ItemEntity entity, Player player) {
        if (player.level().isClientSide) {
            return false;
        }
        ItemStack stack = entity.getItem();
        if (stack.getItem() != ModItems.PORTKEY.get()) {
            return false;
        }
        if (RTNbt.getInt(stack, RTDataKeys.PORTKEY_AGE, 0) <= PRIME_TICKS) {
            return false;
        }
        Optional<PortkeyTarget> targetOpt = PortkeyItem.getTarget(stack);
        if (targetOpt.isEmpty() || !(player instanceof ServerPlayer serverPlayer)
            || !(player.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        PortkeyTarget target = targetOpt.get();
        if (!serverLevel.dimension().equals(target.dimension())) {
            return false; // same-dimension only
        }

        BlockPos targetPos = target.pos();
        List<BlockPos> landing = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int y = targetPos.getY(); y >= targetPos.getY() - 10 && y >= serverLevel.getMinBuildHeight(); y--) {
                    BlockPos check = new BlockPos(targetPos.getX() + dx, y, targetPos.getZ() + dz);
                    if (serverLevel.getBlockState(check).isFaceSturdy(serverLevel, check, Direction.UP)
                        && serverLevel.getBlockState(check.above()).isAir()
                        && serverLevel.getBlockState(check.above(2)).isAir()) {
                        landing.add(check);
                        break;
                    }
                }
            }
        }
        if (landing.isEmpty()) {
            return false;
        }

        Collections.shuffle(landing);
        BlockPos dest = landing.get(0);
        serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        serverPlayer.teleportTo(dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5);
        serverLevel.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        entity.discard();
        return true;
    }
}
