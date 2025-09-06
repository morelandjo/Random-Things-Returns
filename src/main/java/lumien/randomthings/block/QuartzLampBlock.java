package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class QuartzLampBlock extends Block {
    
    public QuartzLampBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .lightLevel((state) -> 15)); // Always emits light level 15
    }

    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        // Server-side: Always emit light level 15
        // Client-side: Return 0 for invisible light
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return 0; // Client sees no light
        } else {
            return 15; // Server calculates with full light
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Periodically check and update lighting on client
        if (level.isClientSide && level.getBrightness(LightLayer.BLOCK, pos) == 15) {
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }
}