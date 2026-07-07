package lumien.randomthings.block;

import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * A re-skinned vanilla crafting table. Aesthetic only — opens the vanilla 3x3 grid and uses the
 * vanilla {@code minecraft:crafting} recipe type (so no recipe-serializer work and no JEI catalyst).
 */
public class CustomCraftingTableBlock extends CraftingTableBlock {

    public CustomCraftingTableBlock() {
        this(Properties.of()
            .strength(2.5F)
            .sound(SoundType.WOOD)
            .ignitedByLava());
    }

    public CustomCraftingTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
