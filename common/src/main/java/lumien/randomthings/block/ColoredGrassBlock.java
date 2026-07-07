package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ColoredGrassBlock extends Block {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

    public ColoredGrassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return InteractionResult.PASS;
        }
        DyeColor newColor = dye.getDyeColor();
        if (state.getValue(COLOR) == newColor) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, state.setValue(COLOR, newColor), 3);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // Decay: if dark above and something opaque on top, revert to dirt.
        if (level.getMaxLocalRawBrightness(abovePos) < 4 && aboveState.getLightBlock(level, abovePos) > 2) {
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return;
        }

        // Spread: convert nearby dirt to our colored grass when lit.
        if (level.getMaxLocalRawBrightness(abovePos) >= 9) {
            for (int i = 0; i < 4; i++) {
                BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                if (!level.isLoaded(target) || !level.isLoaded(target.above())) {
                    return;
                }
                BlockState targetState = level.getBlockState(target);
                BlockState targetAboveState = level.getBlockState(target.above());

                if (targetState.is(Blocks.DIRT)
                        && level.getMaxLocalRawBrightness(target.above()) >= 4
                        && targetAboveState.getLightBlock(level, target.above()) <= 2) {
                    level.setBlockAndUpdate(target, state);
                }
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(itemForColor(state.getValue(COLOR)));
    }

    private static net.minecraft.world.item.Item itemForColor(DyeColor color) {
        return switch (color) {
            case WHITE -> ModItems.COLORED_GRASS_WHITE.get();
            case ORANGE -> ModItems.COLORED_GRASS_ORANGE.get();
            case MAGENTA -> ModItems.COLORED_GRASS_MAGENTA.get();
            case LIGHT_BLUE -> ModItems.COLORED_GRASS_LIGHT_BLUE.get();
            case YELLOW -> ModItems.COLORED_GRASS_YELLOW.get();
            case LIME -> ModItems.COLORED_GRASS_LIME.get();
            case PINK -> ModItems.COLORED_GRASS_PINK.get();
            case GRAY -> ModItems.COLORED_GRASS_GRAY.get();
            case LIGHT_GRAY -> ModItems.COLORED_GRASS_LIGHT_GRAY.get();
            case CYAN -> ModItems.COLORED_GRASS_CYAN.get();
            case PURPLE -> ModItems.COLORED_GRASS_PURPLE.get();
            case BLUE -> ModItems.COLORED_GRASS_BLUE.get();
            case BROWN -> ModItems.COLORED_GRASS_BROWN.get();
            case GREEN -> ModItems.COLORED_GRASS_GREEN.get();
            case RED -> ModItems.COLORED_GRASS_RED.get();
            case BLACK -> ModItems.COLORED_GRASS_BLACK.get();
        };
    }
}
