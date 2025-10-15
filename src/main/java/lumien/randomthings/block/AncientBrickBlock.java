package lumien.randomthings.block;

import com.mojang.serialization.MapCodec;

import lumien.randomthings.blockentity.AncientFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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

public class AncientBrickBlock extends Block {
    public static final MapCodec<AncientBrickBlock> CODEC = simpleCodec(AncientBrickBlock::new);

    @Override
    public MapCodec<AncientBrickBlock> codec() {
        return CODEC;
    }

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
                .strength(-1.0F, 3600000.0F) // Unbreakable
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.RUNES));
    }

    protected AncientBrickBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, Variant.RUNES));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Melt snow on top of ancient bricks
        if (!level.isClientSide && random.nextInt(3) == 0) {
            BlockState upState = level.getBlockState(pos.above());

            if (upState.getBlock() == Blocks.SNOW) {
                level.removeBlock(pos.above(), false);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Check if player is holding a nether star
        if (stack.getItem() == Items.NETHER_STAR) {
            if (state.getValue(VARIANT) == Variant.STAR_EMPTY) {
                if (!level.isClientSide) {
                    // Change to filled star variant
                    level.setBlock(pos, state.setValue(VARIANT, Variant.STAR_FULL), 3);

                    // Check if there's an Ancient Furnace below
                    BlockState stateDown = level.getBlockState(pos.below());
                    if (stateDown.getBlock() == ModBlocks.ANCIENT_FURNACE.get()) {
                        BlockEntity blockEntity = level.getBlockEntity(pos.below());
                        if (blockEntity instanceof AncientFurnaceBlockEntity furnace) {
                            furnace.start();
                        }
                    }

                    // Consume the nether star
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
