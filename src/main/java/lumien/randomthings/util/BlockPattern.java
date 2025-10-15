package lumien.randomthings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for placing multi-block structures in the world.
 * Migrated from 1.12.2 Random Things.
 */
public class BlockPattern {
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final BlockState[][][] pattern;

    public BlockPattern(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.pattern = new BlockState[sizeX][sizeY][sizeZ];
    }

    /**
     * Sets a block state at a specific position in the pattern.
     * @param x X coordinate (0 to sizeX-1)
     * @param y Y coordinate (0 to sizeY-1)
     * @param z Z coordinate (0 to sizeZ-1)
     * @param state The block state to place
     */
    public void setBlockState(int x, int y, int z, BlockState state) {
        if (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ) {
            pattern[x][y][z] = state;
        }
    }

    /**
     * Places the entire pattern in the world at the specified position.
     * The position represents the corner (minX, minY, minZ) of the pattern.
     * @param level The level to place the pattern in
     * @param pos The starting position (corner)
     */
    public void place(LevelAccessor level, BlockPos pos) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    BlockState state = pattern[x][y][z];
                    if (state != null) {
                        level.setBlock(pos.offset(x, y, z), state, 3);
                    }
                }
            }
        }
    }

    /**
     * Checks if the pattern can be placed at the specified position.
     * Returns true if all non-null positions in the pattern can be replaced.
     * @param level The level to check
     * @param pos The starting position
     * @return true if the pattern can be placed
     */
    public boolean canPlace(LevelAccessor level, BlockPos pos) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (pattern[x][y][z] != null) {
                        BlockPos checkPos = pos.offset(x, y, z);
                        BlockState existing = level.getBlockState(checkPos);

                        // Can't place if position is not replaceable
                        if (!existing.canBeReplaced()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public int getSizeZ() {
        return sizeZ;
    }
}
