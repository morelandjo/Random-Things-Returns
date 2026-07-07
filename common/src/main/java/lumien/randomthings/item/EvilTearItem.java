package lumien.randomthings.item;

import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Use on the End Rod of a valid frame to open a temporary portal to the End. */
public class EvilTearItem extends Item {
    public EvilTearItem() {
        super(new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(Blocks.END_ROD)) {
            BlockPos portalCenter = pos.below(3);
            if (ArtificialEndPortalEntity.isValidPosition(level, portalCenter, true)) {
                if (!level.isClientSide) {
                    ArtificialEndPortalEntity portal = new ArtificialEndPortalEntity(
                        level, portalCenter.getX() + 0.5, portalCenter.getY(), portalCenter.getZ() + 0.5);
                    level.addFreshEntity(portal);
                    context.getItemInHand().shrink(1);
                    level.playSound(null, pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
