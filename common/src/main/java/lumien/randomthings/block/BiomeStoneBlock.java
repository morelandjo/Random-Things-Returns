package lumien.randomthings.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

/** Biome-tinted stone with five carved variants (tint applied client-side via a block colour handler). */
public class BiomeStoneBlock extends Block {

    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public BiomeStoneBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(1.5f, 6.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.STONE));
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.SMOOTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    public enum Variant implements StringRepresentable {
        SMOOTH("smooth"),
        COBBLE("cobble"),
        BRICK("brick"),
        CRACKED("cracked"),
        CHISELED("chiseled");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
