package lumien.randomthings.event;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class SlimeBlockConversionHandler {
    
    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = event.getItemStack();
        
        // Check if player right-clicked a vanilla slime block with a shovel
        if (state.is(Blocks.SLIME_BLOCK) && heldItem.getItem() instanceof ShovelItem && !level.isClientSide) {
            // Convert vanilla slime block to compressed slime block (level 0)
            BlockState newState = ModBlocks.COMPRESSED_SLIME_BLOCK.get().defaultBlockState()
                .setValue(lumien.randomthings.block.CompressedSlimeBlock.COMPRESSION, 0);
            
            level.setBlock(pos, newState, 3);
            level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            // Damage the shovel
            if (!event.getEntity().isCreative()) {
                heldItem.hurtAndBreak(1, event.getEntity(), LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
            }
            
            event.setCanceled(true);
        }
    }
}