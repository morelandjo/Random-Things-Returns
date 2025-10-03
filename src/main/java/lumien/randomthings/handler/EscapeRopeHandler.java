package lumien.randomthings.handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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

            if (player != null && player.level() != null) {
                ItemStack holding = player.getUseItem();

                if (!holding.isEmpty() && holding.getItem() == ModItems.ESCAPE_ROPE.get()) {
                    ArrayList<BlockPos> toCheck = task.toCheck;
                    HashSet<BlockPos> alreadyChecked = task.alreadyChecked;
                    Level level = player.level();

                    // Process up to 4 blocks per tick to prevent lag
                    for (int runs = 0; runs < 4; runs++) {
                        boolean finished = false;

                        if (toCheck.isEmpty() || alreadyChecked.size() > 10000) {
                            // Search failed - drop the item and stop using
                            finished = true;
                            player.drop(holding, false);
                            player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
                        } else {
                            BlockPos nextPos = toCheck.remove(toCheck.size() - 1);

                            // Skip already checked positions
                            while (alreadyChecked.contains(nextPos) && !toCheck.isEmpty()) {
                                nextPos = toCheck.remove(toCheck.size() - 1);
                            }

                            if (!alreadyChecked.contains(nextPos)) {
                                // Check if chunk is loaded and position is air or passable
                                if (level.isLoaded(nextPos) &&
                                    (level.isEmptyBlock(nextPos) ||
                                     level.getBlockState(nextPos).getCollisionShape(level, nextPos).isEmpty())) {

                                    if (level.canSeeSky(nextPos)) {
                                        // Found a path to surface! Teleport the player
                                        level.playSound(null, player.blockPosition(),
                                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.0f);

                                        // Find a safe landing spot (solid ground)
                                        boolean foundSolid = false;
                                        for (int y = nextPos.getY(); y >= level.getMinBuildHeight(); y--) {
                                            BlockPos target = new BlockPos(nextPos.getX(), y, nextPos.getZ());
                                            if (level.getBlockState(target).isFaceSturdy(level, target, Direction.UP) ||
                                                level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {

                                                // Teleport player to safe position
                                                player.teleportTo(target.getX() + 0.5, target.getY() + 1, target.getZ() + 0.5);
                                                foundSolid = true;
                                                break;
                                            }
                                        }

                                        if (!foundSolid) {
                                            // No solid ground found, teleport to surface level
                                            player.teleportTo(nextPos.getX() + 0.5, nextPos.getY() + 1, nextPos.getZ() + 0.5);
                                        }

                                        player.stopUsingItem();

                                        // Play teleport sound at destination
                                        level.playSound(null, player.blockPosition(),
                                            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.0f);

                                        // Damage the rope
                                        holding.hurtAndBreak(1, player, player.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND ?
                                            net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
                                        finished = true;
                                    } else {
                                        // Haven't reached surface yet, add adjacent positions to search
                                        alreadyChecked.add(nextPos);

                                        // Add adjacent positions (prioritize going up and out)
                                        for (Direction direction : Direction.values()) {
                                            BlockPos addPos = nextPos.relative(direction);
                                            if (!alreadyChecked.contains(addPos)) {
                                                toCheck.add(addPos);
                                            }
                                        }
                                    }
                                } else {
                                    // Position is blocked, mark as checked
                                    alreadyChecked.add(nextPos);
                                }
                            } else {
                                finished = true;
                            }
                        }

                        if (finished) {
                            iterator.remove();
                            break;
                        }
                    }
                } else {
                    // Player stopped using the item, cancel the task
                    iterator.remove();
                }
            } else {
                // Player disconnected or invalid, remove task
                iterator.remove();
            }
        }
    }

    public static EscapeRopeHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EscapeRopeHandler();
        }
        return INSTANCE;
    }
}