package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Plant a bean to grow a bean sprout. */
public class ItemBean extends Item {
    public ItemBean() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (context.getClickedFace() != Direction.UP || player == null
            || !player.mayUseItemAt(pos.relative(Direction.UP), Direction.UP, itemStack)) {
            return InteractionResult.FAIL;
        }
        BlockPos targetPos = pos.above();
        if (level.isEmptyBlock(targetPos)) {
            BlockState belowState = level.getBlockState(targetPos.below());
            boolean canPlant = belowState.is(net.minecraft.tags.BlockTags.DIRT)
                || belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.FARMLAND);
            if (canPlant && level.getRawBrightness(targetPos, 0) >= 8) {
                level.setBlock(targetPos, ModBlocks.BEANSPROUT.get().defaultBlockState(), 3);
                level.playSound(null, targetPos, level.getBlockState(targetPos).getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.FAIL;
    }
}
