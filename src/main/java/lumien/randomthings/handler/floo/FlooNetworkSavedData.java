package lumien.randomthings.handler.floo;

import lumien.randomthings.block.FlooBrickBlock;
import lumien.randomthings.blockentity.FlooBrickBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Per-dimension persistent registry of all named floo fireplaces.
 * Mirrors the upstream 1.12.2 FlooNetworkHandler at
 * /Users/josh/Documents/minecraftmodding/othermodsgits/Random-Things/src/main/java/lumien/randomthings/handler/floo/FlooNetworkHandler.java
 */
public class FlooNetworkSavedData extends SavedData {
    private static final String DATA_NAME = "randomthings_floo_network";

    private final List<FlooFireplace> fireplaces = new ArrayList<>();

    public FlooNetworkSavedData() {
    }

    public FlooNetworkSavedData(CompoundTag compound, HolderLookup.Provider registries) {
        ListTag list = compound.getList("firePlaces", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            this.fireplaces.add(FlooFireplace.fromNbt(list.getCompound(i)));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (FlooFireplace fp : fireplaces) {
            list.add(fp.toNbt());
        }
        compound.put("firePlaces", list);
        return compound;
    }

    public static FlooNetworkSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new Factory<>(FlooNetworkSavedData::new, FlooNetworkSavedData::new),
            DATA_NAME
        );
    }

    public List<FlooFireplace> getFireplaces() {
        return fireplaces;
    }

    /**
     * Mirrors upstream FlooNetworkHandler.getFirePlaceTE(world, uuid) — returns the saved
     * position of the fireplace's master, or null if no fireplace with that UUID is registered.
     */
    @Nullable
    public BlockPos findMasterPosition(ServerLevel level, UUID masterUUID) {
        for (FlooFireplace fp : fireplaces) {
            if (fp.getMasterUUID().equals(masterUUID)) {
                return fp.getLastKnownPosition();
            }
        }
        return null;
    }

    /**
     * Validates and registers a new fireplace. Returns false if any child position OR the master
     * position overlaps an existing fireplace's master, or if a fireplace with the same
     * (case-insensitive) name already exists. Mirrors upstream FlooNetworkHandler.createFireplace.
     */
    public boolean createFireplace(@Nullable UUID creatorPlayerUUID, UUID masterUUID, @Nullable String name, BlockPos masterPos, List<BlockPos> childPositions) {
        for (FlooFireplace fp : fireplaces) {
            BlockPos existing = fp.getLastKnownPosition();
            if (existing.equals(masterPos) || childPositions.contains(existing)) {
                return false;
            }
            if (name != null && fp.getName() != null && fp.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        this.fireplaces.add(new FlooFireplace(masterUUID, creatorPlayerUUID, name, masterPos));
        this.setDirty();
        return true;
    }

    public void brokenMaster(UUID masterUUID) {
        Iterator<FlooFireplace> it = fireplaces.iterator();
        while (it.hasNext()) {
            if (it.next().getMasterUUID().equals(masterUUID)) {
                it.remove();
                this.setDirty();
                return;
            }
        }
    }

    public void updatePosition(UUID masterUUID, BlockPos newPos) {
        for (FlooFireplace fp : fireplaces) {
            if (fp.getMasterUUID().equals(masterUUID)) {
                if (!fp.getLastKnownPosition().equals(newPos)) {
                    fp.setLastKnownPosition(newPos);
                    this.setDirty();
                }
                return;
            }
        }
    }

    /**
     * Attempt to teleport the player to the named destination. Returns true on success.
     * Recursively retries after pruning stale entries.
     */
    public boolean teleport(ServerLevel level, @Nullable BlockPos originPos, ServerPlayer player, String enteredDestination) {
        if (enteredDestination == null || enteredDestination.isEmpty()) return false;

        FlooFireplace target = null;
        int bestDist = Integer.MAX_VALUE;
        for (FlooFireplace fp : fireplaces) {
            String n = fp.getName();
            if (n == null || n.isEmpty()) continue;
            int d = levenshtein(n.toLowerCase(), enteredDestination.toLowerCase());
            if (d < bestDist) {
                bestDist = d;
                target = fp;
            }
        }

        if (target == null) {
            return false;
        }

        if (originPos != null && target.getLastKnownPosition().equals(originPos)) {
            player.sendSystemMessage(Component.translatable("floo.info.same").withStyle(ChatFormatting.RED));
            return false;
        }

        BlockPos targetPos = target.getLastKnownPosition();
        BlockEntity be = level.getBlockEntity(targetPos);
        if (!(be instanceof FlooBrickBlockEntity flooBE) || !flooBE.isMaster()) {
            // Stale entry — remove and try again
            brokenMaster(target.getMasterUUID());
            return teleport(level, originPos, player, enteredDestination);
        }

        Direction facing = flooBE.getFacing();
        // Upstream stores facing-as-stored (already inverted at sign-use time for vertical clicks)
        // and teleports with tpFacing.getHorizontalAngle() directly — no extra inversion.
        float yaw = facing != null ? facing.toYRot() : 0.0F;

        player.sendSystemMessage(Component.translatable("floo.info.teleport", target.getName()).withStyle(ChatFormatting.GRAY));

        // Origin particles BEFORE teleport (player still there to see them)
        if (originPos != null) {
            spawnParticleBurstAround(level, originPos);
        }

        double tx = targetPos.getX() + 0.5;
        double ty = targetPos.getY() + 1.0;
        double tz = targetPos.getZ() + 0.5;
        player.connection.teleport(tx, ty, tz, yaw, 0.0F);

        // Destination particles AFTER teleport so the now-relocated player gets them.
        spawnParticleBurstAround(level, targetPos);

        return true;
    }

    private static void spawnParticleBurstAround(ServerLevel level, BlockPos masterPos) {
        // 50 particles per brick (master + children), matching upstream MessageFlooParticles.java:70-75.
        // Upstream offsets: x + 0..1, y + 1..2, z + 0..1, with slight upward initial velocity.
        BlockEntity be = level.getBlockEntity(masterPos);
        List<BlockPos> positions = new ArrayList<>();
        positions.add(masterPos);
        if (be instanceof FlooBrickBlockEntity flooBE && flooBE.isMaster()) {
            positions.addAll(flooBE.getChildren());
        }
        for (BlockPos p : positions) {
            level.sendParticles(ParticleTypes.FLAME,
                p.getX() + 0.5, p.getY() + 1.5, p.getZ() + 0.5,
                50, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private static int levenshtein(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) prev[j] = j;
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev; prev = curr; curr = tmp;
        }
        return prev[m];
    }
}
