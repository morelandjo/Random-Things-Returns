package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.Direction;

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
        Direction face = context.getClickedFace();
        
        if (face != Direction.UP) {
            return InteractionResult.FAIL;
        }
        
        if (!player.mayUseItemAt(pos.relative(face), face, itemStack)) {
            return InteractionResult.FAIL;
        }
        
        BlockPos targetPos = pos.above();
        if (level.isEmptyBlock(targetPos)) {
            // Check if bean sprout can be placed (replicate BlockBeanSprout.canSurvive logic)
            BlockPos belowPos = targetPos.below();
            net.minecraft.world.level.block.state.BlockState belowState = level.getBlockState(belowPos);
            boolean canPlant = belowState.is(net.minecraft.tags.BlockTags.DIRT) || 
                             belowState.getBlock() == net.minecraft.world.level.block.Blocks.GRASS_BLOCK ||
                             belowState.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND;
            boolean hasLight = level.getRawBrightness(targetPos, 0) >= 8;
            
            if (canPlant && hasLight) {
                level.setBlock(targetPos, ModBlocks.BEANSPROUT.get().defaultBlockState(), 3);
                
                SoundType soundType = ModBlocks.BEANSPROUT.get().getSoundType(ModBlocks.BEANSPROUT.get().defaultBlockState(), level, targetPos, player);
                level.playSound(null, targetPos, soundType.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        return InteractionResult.FAIL;
    }
}