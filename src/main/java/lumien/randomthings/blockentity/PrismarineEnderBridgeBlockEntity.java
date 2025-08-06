package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PrismarineEnderBridgeBlockEntity extends EnderBridgeBaseBlockEntity {

    public PrismarineEnderBridgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.PRISMARINE_ENDER_BRIDGE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PrismarineEnderBridgeBlockEntity blockEntity) {
        if (!level.isClientSide) {
            // Prismarine Ender Bridge scans 10 blocks per tick (fast)
            blockEntity.performScanning(level, pos, state, 10);
        }
    }
}