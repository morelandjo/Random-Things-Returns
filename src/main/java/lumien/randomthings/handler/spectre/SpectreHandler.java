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
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all Spectre Cubes in the Spectre Dimension.
 * This is a SavedData that persists player cube information across server restarts.
 */
public class SpectreHandler extends SavedData {
    private static final String DATA_NAME = "randomthings_spectre_handler";

    public static final ResourceKey<Level> SPECTRE_DIMENSION = ResourceKey.create(
        Registries.DIMENSION,
        ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "spectre")
    );

    private final Map<UUID, SpectreCube> cubes = new HashMap<>();
    private int positionCounter = 0;
    private final MinecraftServer server;

    public SpectreHandler(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Gets the SpectreHandler instance for the server.
     */
    public static SpectreHandler getInstance(MinecraftServer server) {
        if (server == null) {
            return null;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return null;
        }

        return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<SpectreHandler>(
                () -> new SpectreHandler(server),
                (tag, provider) -> load(server, tag),
                null
            ),
            DATA_NAME
        );
    }

    /**
     * Loads the SpectreHandler from NBT.
     */
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

        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        ListTag cubeList = new ListTag();

        for (SpectreCube cube : cubes.values()) {
            CompoundTag cubeTag = new CompoundTag();
            cube.writeToNBT(cubeTag);
            cubeList.add(cubeTag);
        }

        tag.put("cubes", cubeList);
        tag.putInt("positionCounter", positionCounter);

        return tag;
    }

    /**
     * Gets the Spectre dimension level.
     */
    public ServerLevel getWorld() {
        return server.getLevel(SPECTRE_DIMENSION);
    }

    /**
     * Checks if the given level is the Spectre dimension.
     */
    public static boolean isSpectreDimension(Level level) {
        return level.dimension().equals(SPECTRE_DIMENSION);
    }

    /**
     * Gets a SpectreCube from a position in the world.
     */
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

    /**
     * Teleports a player to their Spectre Cube.
     */
    public void teleportPlayerToSpectreCube(ServerPlayer player) {
        // Save current position
        CompoundTag playerData = player.getPersistentData();
        playerData.putDouble("spectrePosX", player.getX());
        playerData.putDouble("spectrePosY", player.getY());
        playerData.putDouble("spectrePosZ", player.getZ());
        playerData.putString("spectreDimension", player.level().dimension().location().toString());

        UUID uuid = player.getUUID();
        SpectreCube cube;

        if (cubes.containsKey(uuid)) {
            cube = cubes.get(uuid);
        } else {
            cube = generateSpectreCube(uuid);
        }

        BlockPos spawn = cube.getSpawnBlock();
        ServerLevel spectreWorld = getWorld();

        if (spectreWorld == null) {
            // Dimension not loaded, cannot teleport
            return;
        }

        // Teleport to Spectre dimension
        Vec3 destination = new Vec3(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
        DimensionTransition transition = new DimensionTransition(
            spectreWorld,
            destination,
            Vec3.ZERO, // velocity
            player.getYRot(),
            player.getXRot(),
            DimensionTransition.DO_NOTHING
        );
        player.changeDimension(transition);
    }

    /**
     * Generates a new Spectre Cube for a player.
     */
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

    /**
     * Teleports a player back to their original position before entering the Spectre Dimension.
     */
    public void teleportPlayerBack(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();

        if (!playerData.contains("spectrePosX")) {
            // No saved position, respawn player at world spawn
            ServerLevel overworld = server.getLevel(Level.OVERWORLD);
            if (overworld != null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                Vec3 destination = new Vec3(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);
                DimensionTransition transition = new DimensionTransition(
                    overworld,
                    destination,
                    Vec3.ZERO, // velocity
                    player.getYRot(),
                    player.getXRot(),
                    DimensionTransition.DO_NOTHING
                );
                player.changeDimension(transition);
            }
            return;
        }

        double x = playerData.getDouble("spectrePosX");
        double y = playerData.getDouble("spectrePosY");
        double z = playerData.getDouble("spectrePosZ");
        String dimensionId = playerData.getString("spectreDimension");

        // Parse dimension key
        ResourceLocation dimensionLoc = ResourceLocation.tryParse(dimensionId);
        if (dimensionLoc == null) {
            // Invalid dimension, go to overworld spawn
            dimensionLoc = Level.OVERWORLD.location();
        }

        ResourceKey<Level> targetDimension = ResourceKey.create(Registries.DIMENSION, dimensionLoc);
        ServerLevel targetWorld = server.getLevel(targetDimension);

        if (targetWorld == null) {
            // Dimension doesn't exist, go to overworld
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

            // Ensure player isn't stuck in a block by checking upward
            BlockPos checkPos = BlockPos.containing(destination);
            while (!targetWorld.noCollision(player.getBoundingBox().move(destination.subtract(player.position()))) &&
                   checkPos.getY() < targetWorld.getMaxBuildHeight()) {
                destination = destination.add(0, 1.0, 0);
                checkPos = checkPos.above();
            }

            DimensionTransition transition = new DimensionTransition(
                targetWorld,
                destination,
                Vec3.ZERO, // velocity
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.DO_NOTHING
            );
            player.changeDimension(transition);

            // Clear saved position
            playerData.remove("spectrePosX");
            playerData.remove("spectrePosY");
            playerData.remove("spectrePosZ");
            playerData.remove("spectreDimension");
        }
    }

    /**
     * Checks if a player is in the correct cube (security check).
     * If not in creative mode and in wrong cube, teleport them to their own cube or back.
     */
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
