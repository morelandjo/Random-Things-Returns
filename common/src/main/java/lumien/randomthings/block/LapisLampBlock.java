package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * A decorative lamp. The 1.21.1 build used a Forge-only {@code FMLEnvironment} trick to emit light
 * only client-side (so mobs could still spawn in lit areas); that asymmetric-light gimmick has no
 * cross-loader equivalent, so this is a normal full-brightness lamp.
 */
public class LapisLampBlock extends Block {
    public LapisLampBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIRT)
            .strength(0.3F)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 15));
    }
}
