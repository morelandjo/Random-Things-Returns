package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SuperLubricentStoneBlock extends Block {

    public SuperLubricentStoneBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(1.5F, 6.0F)
            .sound(SoundType.STONE)
            .friction(1F / 0.91F));
    }
}