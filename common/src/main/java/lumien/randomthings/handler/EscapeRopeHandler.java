package lumien.randomthings.handler;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Flood-fills from a channeling player toward the sky; on finding an open column it pulls the player
 * up to the surface. Ticked once per server tick (registered from {@code RandomThings.init}).
 */
public class EscapeRopeHandler {
    private static EscapeRopeHandler INSTANCE;
    private final List<Task> runningTasks = new ArrayList<>();

    private static class Task {
        WeakReference<ServerPlayer> player;
        ArrayList<BlockPos> toCheck = new ArrayList<>();
        HashSet<BlockPos> alreadyChecked = new HashSet<>();
    }

    public void addTask(ServerPlayer player) {
        Task task = new Task();
        task.player = new WeakReference<>(player);
        task.toCheck.add(player.blockPosition());
        this.runningTasks.add(task);
    }

    public void tick() {
        Iterator<Task> iterator = runningTasks.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            ServerPlayer player = task.player.get();
            if (player == null || player.level() == null) {
                iterator.remove();
                continue;
            }
            ItemStack holding = player.getUseItem();
            if (holding.isEmpty() || holding.getItem() != ModItems.ESCAPE_ROPE.get()) {
                iterator.remove();
                continue;
            }
            if (step(task, player, holding)) {
                iterator.remove();
            }
        }
    }

    /** Runs up to 4 flood-fill steps; returns true when the task is finished. */
    private boolean step(Task task, ServerPlayer player, ItemStack holding) {
        Level level = player.level();
        for (int runs = 0; runs < 4; runs++) {
            if (task.toCheck.isEmpty() || task.alreadyChecked.size() > 10000) {
                player.drop(holding, false);
                player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
                return true;
            }
            BlockPos nextPos = task.toCheck.remove(task.toCheck.size() - 1);
            while (task.alreadyChecked.contains(nextPos) && !task.toCheck.isEmpty()) {
                nextPos = task.toCheck.remove(task.toCheck.size() - 1);
            }
            if (task.alreadyChecked.contains(nextPos)) {
                return true;
            }
            boolean passable = level.isLoaded(nextPos)
                && (level.isEmptyBlock(nextPos) || level.getBlockState(nextPos).getCollisionShape(level, nextPos).isEmpty());
            if (!passable) {
                task.alreadyChecked.add(nextPos);
                continue;
            }
            if (level.canSeeSky(nextPos)) {
                teleportUp(player, holding, level, nextPos);
                return true;
            }
            task.alreadyChecked.add(nextPos);
            for (Direction direction : Direction.values()) {
                BlockPos addPos = nextPos.relative(direction);
                if (!task.alreadyChecked.contains(addPos)) {
                    task.toCheck.add(addPos);
                }
            }
        }
        return false;
    }

    private void teleportUp(ServerPlayer player, ItemStack holding, Level level, BlockPos surface) {
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.0f);
        BlockPos dest = surface;
        for (int y = surface.getY(); y >= level.getMinBuildHeight(); y--) {
            BlockPos target = new BlockPos(surface.getX(), y, surface.getZ());
            if (level.getBlockState(target).isFaceSturdy(level, target, Direction.UP)
                || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
                dest = target;
                break;
            }
        }
        player.teleportTo(dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5);
        player.stopUsingItem();
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.0f);
        holding.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    public static EscapeRopeHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EscapeRopeHandler();
        }
        return INSTANCE;
    }
}
