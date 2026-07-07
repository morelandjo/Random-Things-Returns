package lumien.randomthings.handler.spectre;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/**
 * A single player's private cube in the Spectre Dimension: a hollow 16x16 room of Spectre Blocks
 * with a 2x2 Spectre Core at the center of the floor; height expandable with Ectoplasm.
 */
public class SpectreCube {
    private UUID owner;
    private int height;
    private int position; // X position in the dimension (each cube gets a 16-block wide chunk)
    private BlockPos spawnBlock;
    private final SpectreHandler handler;

    public SpectreCube(SpectreHandler handler) {
        this.handler = handler;
        this.height = 2;
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
     * Increases the height of the cube.
     * @param amount Number of ectoplasm offered
     * @return Actual amount consumed (limited by the height cap)
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

    /** Generates the cube structure in the world. */
    public void generate(ServerLevel level) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);
        BlockPos pos1 = corner;
        BlockPos pos2 = corner.offset(15, height + 1, 15);

        generateCubeFrame(level, pos1, pos2, ModBlocks.SPECTRE_BLOCK.get().defaultBlockState(), 3);

        // 2x2 core in the center of the floor
        generateCubeFrame(level, pos1.offset(7, 0, 7), pos1.offset(8, 0, 8), ModBlocks.SPECTRE_CORE.get().defaultBlockState(), 3);
    }

    private void changeHeight(ServerLevel level, int newHeight) {
        BlockPos corner = new BlockPos(position * 16, 0, 0);
        BlockPos pos1 = corner;
        BlockPos pos2 = corner.offset(15, height + 1, 15);

        // Clear old structure
        generateCubeFrame(level, pos1, pos2, Blocks.AIR.defaultBlockState(), 2);

        this.height = newHeight;
        generate(level);

        handler.setDirty();
    }

    /** Places a hollow cube of the given state between the two corners. */
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
                    if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ) {
                        level.setBlock(new BlockPos(x, y, z), state, flags);
                    }
                }
            }
        }
    }

    public void writeToNBT(CompoundTag compound) {
        compound.putString("owner", owner.toString());
        compound.putInt("position", position);
        compound.putInt("height", height);
        compound.putInt("spawnX", spawnBlock.getX());
        compound.putInt("spawnY", spawnBlock.getY());
        compound.putInt("spawnZ", spawnBlock.getZ());
    }

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
