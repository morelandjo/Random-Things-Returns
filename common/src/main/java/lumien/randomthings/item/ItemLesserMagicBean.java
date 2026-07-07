package lumien.randomthings.item;

import lumien.randomthings.block.BlockBeanStalk;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Plant a lesser magic bean to grow a climbable beanstalk. */
public class ItemLesserMagicBean extends Item {
    public ItemLesserMagicBean() {
        super(new Item.Properties().rarity(Rarity.UNCOMMON));
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
            Block belowBlock = level.getBlockState(targetPos.below()).getBlock();
            boolean canPlant = belowBlock == Blocks.GRASS_BLOCK || belowBlock == Blocks.DIRT
                || belowBlock == Blocks.COARSE_DIRT || belowBlock == Blocks.PODZOL
                || belowBlock == Blocks.ROOTED_DIRT || belowBlock instanceof BlockBeanStalk;
            if (canPlant) {
                level.setBlock(targetPos, ModBlocks.BEANSTALK.get().defaultBlockState(), 3);
                level.scheduleTick(targetPos, ModBlocks.BEANSTALK.get(), 20);
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
