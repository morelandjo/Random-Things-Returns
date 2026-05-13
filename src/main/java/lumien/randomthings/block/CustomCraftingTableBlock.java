package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class CustomCraftingTableBlock extends CraftingTableBlock {
    public static final MapCodec<CustomCraftingTableBlock> CODEC = simpleCodec(CustomCraftingTableBlock::new);

    public CustomCraftingTableBlock() {
        this(Properties.of()
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .ignitedByLava());
    }

    public CustomCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends CraftingTableBlock> codec() {
        return CODEC;
    }
}
