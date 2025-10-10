package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.BlockEntityRuneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.IntStream;

public class RunePatternItem extends Item {

    public RunePatternItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        int[] runeData = stack.get(ModDataComponents.RUNE_PATTERN.get());

        if (runeData != null && runeData.length == 16) {
            int[] amount = new int[DyeColor.values().length];

            for (int i = 0; i < runeData.length; i++) {
                if (runeData[i] != -1 && runeData[i] < amount.length) {
                    amount[runeData[i]]++;
                }
            }

            List<Map.Entry<DyeColor, Integer>> list = new ArrayList<>();
            for (int i = 0; i < amount.length; i++) {
                if (amount[i] != 0) {
                    list.add(new AbstractMap.SimpleEntry<>(DyeColor.byId(i), amount[i]));
                }
            }

            // Sort by count descending
            list.sort((o1, o2) -> o2.getValue() - o1.getValue());

            for (Map.Entry<DyeColor, Integer> entry : list) {
                tooltipComponents.add(Component.literal("- " + entry.getValue() + "x " +
                    Component.translatable("item.runeDust." + entry.getKey().getName() + ".name").getString()));
            }
        } else {
            tooltipComponents.add(Component.translatable("tooltip.general.empty"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift right-click to convert back to paper
        if (player.isShiftKeyDown() && stack.has(ModDataComponents.RUNE_PATTERN.get())) {
            return InteractionResultHolder.success(new ItemStack(Items.PAPER));
        }

        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        if (player == null) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = player.getItemInHand(context.getHand());

        // If clicking on air above a solid block, try to create the pattern
        if (level.isEmptyBlock(pos.above()) && state.isFaceSturdy(level, pos, Direction.UP)) {
            int[] runeData = stack.get(ModDataComponents.RUNE_PATTERN.get());

            if (runeData != null && runeData.length == 16) {
                int[][] actualRuneData = new int[4][4];
                boolean anyPlaced = player.getAbilities().instabuild;

                for (int i = 0; i < runeData.length; i++) {
                    int x = i % 4;
                    int z = i / 4;
                    int runeType = runeData[i];

                    if (runeType != -1 && !player.getAbilities().instabuild) {
                        boolean available = false;
                        // Search inventory for matching rune dust
                        for (int s = 0; s < player.getInventory().getContainerSize(); s++) {
                            ItemStack invStack = player.getInventory().getItem(s);

                            if (!invStack.isEmpty() && invStack.getItem() == ModItems.RUNE_DUST.get()) {
                                DyeColor color = invStack.get(ModDataComponents.RUNE_COLOR.get());
                                if (color != null && color.getId() == runeType) {
                                    if (!level.isClientSide) {
                                        invStack.shrink(1);
                                    }
                                    available = true;
                                    break;
                                }
                            }
                        }

                        if (!available) {
                            runeType = -1;
                        } else {
                            anyPlaced = true;
                        }
                    }

                    actualRuneData[x][z] = runeType;
                }

                if (anyPlaced) {
                    if (!level.isClientSide) {
                        level.setBlock(pos.above(), ModBlocks.RUNE_BASE.get().defaultBlockState(), 3);
                        BlockEntityRuneBase blockEntity = (BlockEntityRuneBase) level.getBlockEntity(pos.above());
                        if (blockEntity != null) {
                            blockEntity.setRuneData(actualRuneData);
                            blockEntity.setChanged();
                        }
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.FAIL;
    }
}
