package lumien.randomthings.block;

import lumien.randomthings.blockentity.AncientFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Unbreakable ancient brick with decorative variants; melts snow resting on it. Right-clicking the
 * empty-star variant with a Nether Star fills it and starts the Ancient Furnace below.
 */
public class AncientBrickBlock extends Block {

    public enum Variant implements StringRepresentable {
        RUNES("runes"),
        DEFAULT("default"),
        STAR_EMPTY("empty"),
        STAR_FULL("full"),
        OUTPUT("output");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    public AncientBrickBlock() {
        super(BlockBehaviour.Properties.of()
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.RUNES));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide && random.nextInt(3) == 0 && level.getBlockState(pos.above()).is(Blocks.SNOW)) {
            level.removeBlock(pos.above(), false);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.NETHER_STAR && state.getValue(VARIANT) == Variant.STAR_EMPTY) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(VARIANT, Variant.STAR_FULL), 3);

                // Start the Ancient Furnace below, if present
                if (level.getBlockState(pos.below()).getBlock() == ModBlocks.ANCIENT_FURNACE.get()) {
                    BlockEntity blockEntity = level.getBlockEntity(pos.below());
                    if (blockEntity instanceof AncientFurnaceBlockEntity furnace) {
                        furnace.start();
                    }
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }
}
