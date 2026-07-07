package lumien.randomthings.item;

import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Hangs an Eclipsed Clock on a wall (like an item frame). */
public class EclipsedClockItem extends Item {

    public EclipsedClockItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && !mayPlace(player, direction, stack, pos)) {
            return InteractionResult.FAIL;
        }
        Level level = context.getLevel();
        EclipsedClockEntity clock = new EclipsedClockEntity(level, pos, direction);
        if (clock.survives()) {
            if (!level.isClientSide) {
                clock.playPlacementSound();
                level.addFreshEntity(clock);
            }
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    private boolean mayPlace(Player player, Direction direction, ItemStack stack, BlockPos pos) {
        return !direction.getAxis().isVertical() && player.mayUseItemAt(pos, direction, stack) && player.level().isInWorldBounds(pos);
    }
}
