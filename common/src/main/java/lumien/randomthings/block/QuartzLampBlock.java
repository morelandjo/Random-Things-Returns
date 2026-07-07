package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * A decorative lamp. (The 1.21.1 build used a Forge-only {@code FMLEnvironment} server-only-light
 * trick; no cross-loader equivalent, so this is a normal full-brightness lamp.)
 */
public class QuartzLampBlock extends Block {
    public QuartzLampBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 15));
    }
}
