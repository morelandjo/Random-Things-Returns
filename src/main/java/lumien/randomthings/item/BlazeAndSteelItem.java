package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;

public class BlazeAndSteelItem extends Item {

    public BlazeAndSteelItem(Properties properties) {
        super(properties.stacksTo(1).durability(64));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        
        // Get the position to place fire at
        pos = pos.relative(context.getClickedFace());

        // Check if player can edit this location
        if (player != null && !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }

        // Check if the position is air
        if (level.isEmptyBlock(pos)) {
            // Play sound
            level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 
                           1.0F, level.random.nextFloat() * 0.8F + 0.8F);
            
            // Place blazing fire
            level.setBlock(pos, ModBlocks.BLAZING_FIRE.get().defaultBlockState(), 11);
            
            // Damage the item
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}