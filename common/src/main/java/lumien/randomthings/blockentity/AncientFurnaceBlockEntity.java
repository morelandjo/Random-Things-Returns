package lumien.randomthings.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import lumien.randomthings.lib.AncientFurnaceConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * The core of the Ancient Furnace multiblock. Activated with a Nether Star, it warms up (breaking a
 * 5x5x5 shell around itself), then flood-fills outward converting cold biomes to their warm
 * equivalents, and finally explodes when done (or after {@link #TRANSFORM_LIMIT} columns).
 */
public class AncientFurnaceBlockEntity extends BlockEntity {

    public enum State {
        IDLE, STARTING, RUNNING
    }

    private static final int TRANSFORM_LIMIT = 10000;
    private static final Direction[] HORIZONTALS = new Direction[] {
        Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    private State state = State.IDLE;

    // STARTING phase data
    private int startingCounter = 0;
    private List<BlockPos> toBreak = new ArrayList<>();

    // RUNNING phase data
    private LinkedHashMap<BlockPos, boolean[]> nextCheckEntries = new LinkedHashMap<>();
    private long transformCount = 0;

    public AncientFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.ANCIENT_FURNACE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("state", state.ordinal());

        if (state == State.STARTING) {
            tag.putInt("startingCounter", startingCounter);

            ListTag toBreakList = new ListTag();
            for (BlockPos pos : toBreak) {
                toBreakList.add(NbtUtils.writeBlockPos(pos));
            }
            tag.put("toBreak", toBreakList);
        }

        if (state == State.RUNNING) {
            ListTag entriesList = new ListTag();
            for (Entry<BlockPos, boolean[]> entry : nextCheckEntries.entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.put("pos", NbtUtils.writeBlockPos(entry.getKey()));

                boolean[] facings = entry.getValue();
                for (int i = 0; i < facings.length; i++) {
                    entryTag.putBoolean("facing" + i, facings[i]);
                }

                entriesList.add(entryTag);
            }
            tag.put("nextCheckEntries", entriesList);
            tag.putLong("transformCount", transformCount);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.state = State.values()[tag.getInt("state")];

        if (state == State.STARTING) {
            this.startingCounter = tag.getInt("startingCounter");

            toBreak = new ArrayList<>();
            ListTag toBreakList = tag.getList("toBreak", 10);
            for (int i = 0; i < toBreakList.size(); i++) {
                toBreak.add(NbtUtils.readBlockPos(toBreakList.getCompound(i)));
            }
        }

        if (state == State.RUNNING) {
            nextCheckEntries = new LinkedHashMap<>();
            ListTag entriesList = tag.getList("nextCheckEntries", 10);

            for (int i = 0; i < entriesList.size(); i++) {
                CompoundTag entryTag = entriesList.getCompound(i);
                BlockPos pos = NbtUtils.readBlockPos(entryTag.getCompound("pos"));

                boolean[] facings = new boolean[HORIZONTALS.length];
                for (int j = 0; j < facings.length; j++) {
                    facings[j] = entryTag.getBoolean("facing" + j);
                }

                nextCheckEntries.put(pos, facings);
            }

            this.transformCount = tag.getLong("transformCount");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AncientFurnaceBlockEntity blockEntity) {
        if (blockEntity.state == State.STARTING) {
            // Advance the warmup counter locally so the renderer's fade progresses (synced on state changes)
            blockEntity.startingCounter = Math.min(20 * 20, blockEntity.startingCounter + 1);
        }
        if (blockEntity.state == State.RUNNING || blockEntity.state == State.STARTING) {
            // Spawn flame particles below the furnace core
            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.2 + level.random.nextDouble() * 0.6;
                double y = pos.getY() - 1;
                double z = pos.getZ() + 0.2 + level.random.nextDouble() * 0.6;
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, -(level.random.nextDouble() * 0.2), 0.0D);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AncientFurnaceBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (blockEntity.state == State.STARTING) {
            blockEntity.tickStarting(serverLevel);
        } else if (blockEntity.state == State.RUNNING) {
            blockEntity.tickRunning(serverLevel);
        }
    }

    private void tickStarting(ServerLevel level) {
        startingCounter = Math.min(20 * 20, startingCounter + 1); // 20 second warmup (400 ticks)

        // Break shell blocks gradually (every 4 ticks)
        if (!toBreak.isEmpty() && startingCounter % 4 == 0) {
            BlockPos nextBreak = toBreak.remove(toBreak.size() - 1);

            if (!level.isEmptyBlock(nextBreak)) {
                level.removeBlock(nextBreak, false);

                level.playSound(null, nextBreak, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                    0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
                level.levelEvent(2000, nextBreak, 4);
            }
        }

        if (startingCounter >= 20 * 20) {
            run();
        }

        setChanged();
    }

    private void tickRunning(ServerLevel level) {
        if (!nextCheckEntries.isEmpty() && transformCount <= TRANSFORM_LIMIT) {
            // Process one position per tick
            Iterator<Entry<BlockPos, boolean[]>> iterator = nextCheckEntries.entrySet().iterator();
            Entry<BlockPos, boolean[]> nextEntry = iterator.next();
            iterator.remove();

            BlockPos nextPos = nextEntry.getKey();

            ResourceKey<Biome> biomeKey = level.getBiome(nextPos).unwrapKey().orElse(null);
            if (biomeKey == null) return;

            ResourceKey<Biome> conversion = AncientFurnaceConversion.getHeatingConversion(biomeKey);

            if (conversion != null) {
                setBiome(level, nextPos, conversion);
                transformCount++;

                // Remove snow / melt ice at the surface
                BlockPos topPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(nextPos.getX(), 0, nextPos.getZ()));
                BlockState topState = level.getBlockState(topPos);

                if (topState.getBlock() == Blocks.SNOW) {
                    level.removeBlock(topPos, false);
                } else if (topState.getBlock() == Blocks.ICE) {
                    level.setBlockAndUpdate(topPos, Blocks.WATER.defaultBlockState());
                }

                // Spread to unvisited neighbors
                for (int i = 0; i < HORIZONTALS.length; i++) {
                    if (nextEntry.getValue()[i]) {
                        Direction facing = HORIZONTALS[i];
                        BlockPos addingPos = nextPos.relative(facing);

                        if (nextCheckEntries.containsKey(addingPos)) {
                            // Mark opposite direction as already checked
                            nextCheckEntries.get(addingPos)[getOppositeIndex(i)] = false;
                        } else {
                            boolean[] newArray = new boolean[HORIZONTALS.length];
                            for (int j = 0; j < newArray.length; j++) {
                                newArray[j] = true;
                            }
                            newArray[getOppositeIndex(i)] = false;
                            nextCheckEntries.put(addingPos, newArray);
                        }
                    }
                }
            }
        } else {
            explodeAndDestroy(level);
        }

        setChanged();
    }

    private int getOppositeIndex(int index) {
        // NORTH=0 <-> SOUTH=1, EAST=2 <-> WEST=3
        if (index == 0) return 1;
        if (index == 1) return 0;
        if (index == 2) return 3;
        return 2;
    }

    private void explodeAndDestroy(ServerLevel level) {
        // Destroy the 3x3x3 multiblock
        for (int modX = -1; modX <= 1; modX++) {
            for (int modY = -1; modY <= 1; modY++) {
                for (int modZ = -1; modZ <= 1; modZ++) {
                    level.removeBlock(worldPosition.offset(modX, modY, modZ), false);
                }
            }
        }

        level.explode(null,
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5,
            4.0F,
            Level.ExplosionInteraction.TNT);
    }

    /**
     * Actually rewrites the biome for the column at {@code pos} (quart resolution), the same way
     * vanilla's /fillbiome does, and resends the chunk's biomes to clients. (The 1.21.1 source
     * stubbed this out; 1.20.1 has the full machinery.)
     */
    private void setBiome(ServerLevel level, BlockPos pos, ResourceKey<Biome> biomeKey) {
        Holder<Biome> biome = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biomeKey);

        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);

        int quartX = QuartPos.fromBlock(pos.getX());
        int quartZ = QuartPos.fromBlock(pos.getZ());

        chunk.fillBiomesFromNoise(
            (x, y, z, sampler) -> (x == quartX && z == quartZ) ? biome : chunk.getNoiseBiome(x, y, z),
            level.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    public void start() {
        if (this.state == State.IDLE) {
            this.state = State.STARTING;
            this.startingCounter = 0;

            // Build list of blocks to break (5x5x5 shell)
            toBreak = new ArrayList<>();
            for (int modX = -2; modX <= 2; modX++) {
                for (int modZ = -2; modZ <= 2; modZ++) {
                    for (int modY = -2; modY <= 2; modY++) {
                        if (Math.abs(modX) == 2 || Math.abs(modZ) == 2 || Math.abs(modY) == 2) {
                            toBreak.add(this.worldPosition.offset(modX, modY, modZ));
                        }
                    }
                }
            }

            Collections.shuffle(toBreak);
            setChanged();
            syncToClients();
        }
    }

    private void run() {
        this.state = State.RUNNING;
        nextCheckEntries = new LinkedHashMap<>();

        // Flood fill starts at the furnace position
        boolean[] initialFacings = new boolean[HORIZONTALS.length];
        for (int i = 0; i < initialFacings.length; i++) {
            initialFacings[i] = true;
        }
        nextCheckEntries.put(this.worldPosition, initialFacings);

        transformCount = 0;
        setChanged();
        syncToClients();
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public State getState() {
        return state;
    }

    public int getStartingCounter() {
        return startingCounter;
    }
}
