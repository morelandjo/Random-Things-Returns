package lumien.randomthings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for placing multi-block structures in the world.
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

    public void setBlockState(int x, int y, int z, BlockState state) {
        if (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ) {
            pattern[x][y][z] = state;
        }
    }

    /** Places the pattern with {@code pos} as its (minX, minY, minZ) corner. */
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

    /** True if every non-null pattern position is replaceable at the target location. */
    public boolean canPlace(LevelAccessor level, BlockPos pos) {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    if (pattern[x][y][z] != null && !level.getBlockState(pos.offset(x, y, z)).canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
