package lumien.randomthings.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnderBridgeBlockEntity extends EnderBridgeBaseBlockEntity {

    public EnderBridgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.ENDER_BRIDGE.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnderBridgeBlockEntity blockEntity) {
        if (!level.isClientSide) {
            // Regular Ender Bridge scans 1 block per tick (slow)
            blockEntity.performScanning(level, pos, state, 1);
        }
    }
}