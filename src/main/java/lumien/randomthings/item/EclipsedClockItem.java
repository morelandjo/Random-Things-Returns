package lumien.randomthings.item;

import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EclipsedClockItem extends Item {
    
    public EclipsedClockItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockpos1 = blockpos; // Pass the clicked block as attachment point
        Player player = context.getPlayer();
        ItemStack itemstack = context.getItemInHand();
        
        if (player != null && !this.mayPlace(player, direction, itemstack, blockpos1)) {
            return InteractionResult.FAIL;
        } else {
            Level level = context.getLevel();
            
            // Create entity at the offset position (like vanilla ItemFrame)
            EclipsedClockEntity hangingentity = new EclipsedClockEntity(level, blockpos1, direction);
            
            // Entity created and positioned outside the clicked face
            
            if (hangingentity.survives()) {
                if (!level.isClientSide) {
                    hangingentity.playPlacementSound();
                    level.addFreshEntity(hangingentity);
                }
                
                itemstack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }
    
    protected boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack) && player.level().isInWorldBounds(pos);
    }
    
    // Property function to get the time value for model variants
    public static ClampedItemPropertyFunction getTimePropertyFunction() {
        return (itemStack, level, entity, i) -> {
            int time = 6000; // Default time (noon)
            
            if (itemStack.has(ModDataComponents.TARGET_TIME)) {
                time = itemStack.get(ModDataComponents.TARGET_TIME);
            }
            
            int i1 = (int)(time % 24000L);
            float f = ((float)i1) / 24000.0F - 0.25F;

            if (f < 0.0F) {
                ++f;
            }

            if (f > 1.0F) {
                --f;
            }

            float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2.0D);
            f = f + (f1 - f) / 3.0F;
            return f;
        };
    }
}