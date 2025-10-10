package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.blockentity.BlockEntityRuneBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;

public class RuneDustItem extends Item {

    public RuneDustItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        BlockHitResult hitResult = new BlockHitResult(context.getClickLocation(), facing, pos, context.isInside());

        if (!level.isClientSide && facing == Direction.UP && player != null) {
            ItemStack stack = player.getItemInHand(hand);
            DyeColor color = stack.get(ModDataComponents.RUNE_COLOR.get());

            if (color == null) {
                return InteractionResult.FAIL;
            }

            BlockState targetState = level.getBlockState(pos);
            BlockEntityRuneBase blockEntity = null;

            // Check if clicking on existing rune base
            if (targetState.getBlock() == ModBlocks.RUNE_BASE.get()) {
                blockEntity = (BlockEntityRuneBase) level.getBlockEntity(pos);
            } else {
                // Try to place new rune base on top of solid block
                if (targetState.isFaceSturdy(level, pos, Direction.UP)) {
                    BlockPos replacePos = pos.offset(facing.getNormal());
                    BlockState toReplace = level.getBlockState(replacePos);

                    if (toReplace.isAir() || toReplace.canBeReplaced()) {
                        level.setBlock(replacePos, ModBlocks.RUNE_BASE.get().defaultBlockState(), 3);
                        blockEntity = (BlockEntityRuneBase) level.getBlockEntity(replacePos);
                    }
                }
            }

            if (blockEntity != null) {
                int[][] runeData = blockEntity.getRuneData();

                // Calculate which cell in the 4x4 grid was clicked
                double hitX = context.getClickLocation().x - pos.getX();
                double hitZ = context.getClickLocation().z - pos.getZ();

                int x = (int) Math.floor(hitX * 4);
                int z = (int) Math.floor(hitZ * 4);

                // Ensure within bounds
                if (x >= 0 && x < 4 && z >= 0 && z < 4) {
                    if (runeData[x][z] == -1) {
                        runeData[x][z] = color.getId();
                        blockEntity.setChanged();
                        level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(),
                            blockEntity.getBlockState(), 3);

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }

                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return super.useOn(context);
    }
}
