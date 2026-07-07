package lumien.randomthings.handler.spectre;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all Spectre Cubes in the Spectre Dimension. SavedData on the overworld, so cube
 * assignments and per-player return positions persist across restarts.
 */
public class SpectreHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_spectre_handler";

    public static final ResourceKey<Level> SPECTRE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(ModConstants.MOD_ID, "spectre")
    );

    private final Map<UUID, SpectreCube> cubes = new HashMap<>();
    /** Where each player entered the dimension from (dimension id + position); persisted. */
    private final Map<UUID, CompoundTag> returnPositions = new HashMap<>();
    private int positionCounter = 0;
    private final MinecraftServer server;

    public SpectreHandler(MinecraftServer server) {
        this.server = server;
    }

    public static SpectreHandler getInstance(MinecraftServer server) {
        if (server == null) {
            return null;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return null;
        }

        return overworld.getDataStorage().computeIfAbsent(
            tag -> load(server, tag),
            () -> new SpectreHandler(server),
            DATA_NAME
        );
    }

    private static SpectreHandler load(MinecraftServer server, CompoundTag tag) {
        SpectreHandler handler = new SpectreHandler(server);

        ListTag cubeList = tag.getList("cubes", Tag.TAG_COMPOUND);
        for (int i = 0; i < cubeList.size(); i++) {
            CompoundTag cubeTag = cubeList.getCompound(i);
            SpectreCube cube = new SpectreCube(handler);
            cube.readFromNBT(cubeTag);
            handler.cubes.put(cube.getOwner(), cube);
        }

        handler.positionCounter = tag.getInt("positionCounter");

        CompoundTag returns = tag.getCompound("returnPositions");
        for (String key : returns.getAllKeys()) {
            handler.returnPositions.put(UUID.fromString(key), returns.getCompound(key));
        }

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag cubeList = new ListTag();

        for (SpectreCube cube : cubes.values()) {
            CompoundTag cubeTag = new CompoundTag();
            cube.writeToNBT(cubeTag);
            cubeList.add(cubeTag);
        }

        tag.put("cubes", cubeList);
        tag.putInt("positionCounter", positionCounter);

        CompoundTag returns = new CompoundTag();
        for (Map.Entry<UUID, CompoundTag> entry : returnPositions.entrySet()) {
            returns.put(entry.getKey().toString(), entry.getValue());
        }
        tag.put("returnPositions", returns);

        return tag;
    }

    public ServerLevel getWorld() {
        return server.getLevel(SPECTRE_DIMENSION);
    }

    public static boolean isSpectreDimension(Level level) {
        return level.dimension().equals(SPECTRE_DIMENSION);
    }

    public SpectreCube getSpectreCubeFromPos(Level level, BlockPos pos) {
        if (!isSpectreDimension(level)) {
            return null;
        }

        if (pos.getZ() > 16 || pos.getZ() < 0) {
            return null;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        int position = chunkPos.x / 16;

        for (SpectreCube cube : cubes.values()) {
            if (cube.getPosition() / 16 == position) {
                if (pos.getY() <= 0 || pos.getY() > cube.getHeight() + 1 ||
                    pos.getX() < position * 16 || pos.getX() > cube.getPosition() * 16 + 15) {
                    return null;
                } else {
                    return cube;
                }
            }
        }

        return null;
    }

    /** Teleports a player to their Spectre Cube, generating one on first use. */
    public void teleportPlayerToSpectreCube(ServerPlayer player) {
        ServerLevel spectreWorld = getWorld();
        if (spectreWorld == null) {
            return;
        }

        // Save current position for the way back (in the SavedData; getPersistentData is Forge-only)
        CompoundTag returnTag = new CompoundTag();
        returnTag.putDouble("x", player.getX());
        returnTag.putDouble("y", player.getY());
        returnTag.putDouble("z", player.getZ());
        returnTag.putString("dimension", player.level().dimension().location().toString());
        returnPositions.put(player.getUUID(), returnTag);
        setDirty();

        UUID uuid = player.getUUID();
        SpectreCube cube = cubes.containsKey(uuid) ? cubes.get(uuid) : generateSpectreCube(uuid);

        BlockPos spawn = cube.getSpawnBlock();
        player.teleportTo(spectreWorld,
            spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5,
            player.getYRot(), player.getXRot());
    }

    private SpectreCube generateSpectreCube(UUID uuid) {
        SpectreCube cube = new SpectreCube(this, uuid, positionCounter);

        positionCounter += 16;

        ServerLevel spectreWorld = getWorld();
        if (spectreWorld != null) {
            cube.generate(spectreWorld);
        }

        cubes.put(uuid, cube);
        setDirty();

        return cube;
    }

    /** Teleports a player back to where they entered the Spectre Dimension from. */
    public void teleportPlayerBack(ServerPlayer player) {
        CompoundTag returnTag = returnPositions.get(player.getUUID());

        if (returnTag == null) {
            // No saved position — send to overworld spawn
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld != null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                player.teleportTo(overworld,
                    spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5,
                    player.getYRot(), player.getXRot());
            }
            return;
        }

        double x = returnTag.getDouble("x");
        double y = returnTag.getDouble("y");
        double z = returnTag.getDouble("z");
        String dimensionId = returnTag.getString("dimension");

        ResourceLocation dimensionLoc = ResourceLocation.tryParse(dimensionId);
        if (dimensionLoc == null) {
            dimensionLoc = Level.OVERWORLD.location();
        }

        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, dimensionLoc);
        ServerLevel targetWorld = server.getLevel(targetDimension);

        if (targetWorld == null) {
            targetWorld = server.getLevel(Level.OVERWORLD);
            if (targetWorld != null) {
                BlockPos spawn = targetWorld.getSharedSpawnPos();
                x = spawn.getX() + 0.5;
                y = spawn.getY() + 1;
                z = spawn.getZ() + 0.5;
            }
        }

        if (targetWorld != null) {
            Vec3 destination = new Vec3(x, y, z);

            // Nudge upward if the destination is obstructed
            BlockPos checkPos = BlockPos.containing(destination);
            while (!targetWorld.noCollision(player.getBoundingBox().move(destination.subtract(player.position())))
                   && checkPos.getY() < targetWorld.getMaxBuildHeight()) {
                destination = destination.add(0, 1.0, 0);
                checkPos = checkPos.above();
            }

            player.teleportTo(targetWorld,
                destination.x, destination.y, destination.z,
                player.getYRot(), player.getXRot());

            returnPositions.remove(player.getUUID());
            setDirty();
        }
    }

    /** Kicks non-creative players out of cubes they don't own. */
    public void checkPosition(ServerPlayer player) {
        SpectreCube cube = getSpectreCubeFromPos(player.level(), player.blockPosition());

        if (!player.isCreative() && (cube == null || !cube.getOwner().equals(player.getUUID()))) {
            SpectreCube playerCube = cubes.get(player.getUUID());

            if (playerCube != null) {
                BlockPos spawn = playerCube.getSpawnBlock();
                player.teleportTo(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
            } else {
                teleportPlayerBack(player);
            }
        }
    }
}
