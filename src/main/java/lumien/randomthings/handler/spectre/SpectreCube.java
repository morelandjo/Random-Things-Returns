package lumien.randomthings.handler.spectre;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * Represents a single player's private cube in the Spectre Dimension.
 * Each cube is a 16x16 room with configurable height (initially 4 blocks, expandable with Ectoplasm).
 */
public class SpectreCube {
    private UUID owner;
    private int height;
    private int position; // X position in the dimension (each cube gets a 16-block wide chunk)
    private BlockPos spawnBlock;
    private SpectreHandler handler;

    public SpectreCube(SpectreHandler handler) {
        this.handler = handler;
        this.height = 2; // Initial height (4 blocks total including floor and ceiling)
    }

    public SpectreCube(SpectreHandler handler, UUID owner, int position) {
        this(handler);
        this.owner = owner;
        this.position = position;
        this.spawnBlock = new BlockPos(position * 16 + 8, 0, 8);
    }

    public UUID getOwner() {
        return owner;
    }

    public int getHeight() {
        return height;
    }

    public int getPosition() {
        return position;
    }

    public BlockPos getSpawnBlock() {
        return spawnBlock;
    }

    public void setSpawnBlock(BlockPos spawnBlock) {
        this.spawnBlock = spawnBlock;
        if (handler != null) {
            handler.setDirty();
        }
    }

    /**
     * Increases the height of the cube by the given amount.
     * @param amount Number of ectoplasm to consume
     * @return Actual amount consumed (limited by height limit)
     */
    public int increaseHeight(int amount) {
        int heightLeft = 255 - (height + 1);
        int difference = heightLeft - amount;
        int newHeight;

        if (difference > 0) {
            newHeight = height + amount;
        } else {
            newHeight = height + heightLeft;
        }

        int change = newHeight - height;

        if (newHeight != height && handler != null) {
            changeHeight(handler.getWorld(), newHeight);
        }

        return change;
    }

    /**
     * Generates the cube structure in the world.
     */
    public void generate(ServerLevel level) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);
        BlockPos pos1 = corner;
        BlockPos pos2 = corner.offset(15, height + 1, 15);

        // Generate the outer walls
        generateCubeFrame(level, pos1, pos2, ModBlocks.SPECTRE_BLOCK.get().defaultBlockState(), 3);

        // Generate the 2x2 core in the center
        generateCubeFrame(level, pos1.offset(7, 0, 7), pos1.offset(8, 0, 8), ModBlocks.SPECTRE_CORE.get().defaultBlockState(), 3);
    }

    /**
     * Changes the height of the cube and regenerates it.
     */
    private void changeHeight(ServerLevel level, int newHeight) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);
        BlockPos pos1 = corner;
        BlockPos pos2 = corner.offset(15, height + 1, 15);

        // Clear old structure
        generateCubeFrame(level, pos1, pos2, Blocks.AIR.defaultBlockState(), 2);

        // Update height
        this.height = newHeight;

        // Regenerate with new height
        generate(level);

        handler.setDirty();
    }

    /**
     * Generates a hollow cube frame with the given block state.
     */
    private static void generateCubeFrame(ServerLevel level, BlockPos pos1, BlockPos pos2, BlockState state, int flags) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());

        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Only place blocks on the edges (hollow cube)
                    if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ) {
                        level.setBlock(new BlockPos(x, y, z), state, flags);
                    }
                }
            }
        }
    }

    /**
     * Writes this cube's data to NBT.
     */
    public void writeToNBT(CompoundTag compound) {
        compound.putString("owner", owner.toString());
        compound.putInt("position", position);
        compound.putInt("height", height);
        compound.putInt("spawnX", spawnBlock.getX());
        compound.putInt("spawnY", spawnBlock.getY());
        compound.putInt("spawnZ", spawnBlock.getZ());
    }

    /**
     * Reads this cube's data from NBT.
     */
    public void readFromNBT(CompoundTag compound) {
        this.owner = UUID.fromString(compound.getString("owner"));
        this.position = compound.getInt("position");
        this.height = compound.getInt("height");
        this.spawnBlock = new BlockPos(
            compound.getInt("spawnX"),
            compound.getInt("spawnY"),
            compound.getInt("spawnZ")
        );
    }
}
