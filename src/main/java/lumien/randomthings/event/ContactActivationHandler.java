package lumien.randomthings.event;

import lumien.randomthings.block.ContactButtonBlock;
import lumien.randomthings.block.ContactLeverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class ContactActivationHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos clickedPos = event.getPos();
        
        // Only process on server side and main hand (matching 1.12 logic)
        if (level.isClientSide || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        
        // Check all 6 directions around the clicked block for contact buttons/levers
        for (Direction direction : Direction.values()) {
            BlockPos contactPos = clickedPos.relative(direction);
            BlockState contactState = level.getBlockState(contactPos);
            Block contactBlock = contactState.getBlock();
            
            // Check if it's a contact button or lever
            if (contactBlock instanceof ContactButtonBlock contactButton) {
                // Check if the contact block is facing the clicked block
                if (contactState.hasProperty(ContactButtonBlock.FACING)) {
                    Direction facing = contactState.getValue(ContactButtonBlock.FACING);
                    
                    // If the contact block is facing the clicked block, activate it
                    if (facing == direction.getOpposite()) {
                        contactButton.activate(level, contactPos, direction.getOpposite());
                    }
                }
            } else if (contactBlock instanceof ContactLeverBlock contactLever) {
                // Check if the contact block is facing the clicked block
                if (contactState.hasProperty(ContactLeverBlock.FACING)) {
                    Direction facing = contactState.getValue(ContactLeverBlock.FACING);
                    
                    // If the contact block is facing the clicked block, activate it
                    if (facing == direction.getOpposite()) {
                        contactLever.activate(level, contactPos, direction.getOpposite());
                    }
                }
            }
        }
    }
}