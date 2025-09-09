package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.core.Direction;

public class ItemMagicBean extends Item {
    
    public ItemMagicBean() {
        super(new Item.Properties().rarity(Rarity.RARE));
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
            // Check if special bean stalk can be placed (replicate BlockSpecialBeanStalk.canSurvive logic)
            BlockPos belowPos = targetPos.below();
            net.minecraft.world.level.block.state.BlockState belowState = level.getBlockState(belowPos);
            net.minecraft.world.level.block.Block belowBlock = belowState.getBlock();
            boolean canPlant = belowBlock == net.minecraft.world.level.block.Blocks.GRASS_BLOCK || 
                             belowBlock == net.minecraft.world.level.block.Blocks.DIRT || 
                             belowBlock == net.minecraft.world.level.block.Blocks.COARSE_DIRT ||
                             belowBlock == net.minecraft.world.level.block.Blocks.PODZOL ||
                             belowBlock == net.minecraft.world.level.block.Blocks.ROOTED_DIRT ||
                             belowBlock instanceof lumien.randomthings.block.BlockSpecialBeanStalk;
            
            if (canPlant) {
                level.setBlock(targetPos, ModBlocks.SPECIALBEANSTALK.get().defaultBlockState(), 3);
                
                // Schedule the magic beanstalk to start growing
                level.scheduleTick(targetPos, ModBlocks.SPECIALBEANSTALK.get(), 20);
                
                SoundType soundType = ModBlocks.SPECIALBEANSTALK.get().getSoundType(ModBlocks.SPECIALBEANSTALK.get().defaultBlockState(), level, targetPos, player);
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