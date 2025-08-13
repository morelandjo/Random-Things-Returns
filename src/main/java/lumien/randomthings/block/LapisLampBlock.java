package lumien.randomthings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

public class LapisLampBlock extends Block {
    
    public LapisLampBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.3F)
                .sound(SoundType.GLASS));
    }
    
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        // Only emit light on client side - allows server-side mob spawning
        if (FMLEnvironment.dist.isClient()) {
            return 15;
        }
        return 0;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Force light update on client when needed - simplified for 1.21.1
        if (level.isClientSide) {
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }
}