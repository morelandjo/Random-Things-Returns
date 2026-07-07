package lumien.randomthings.item;

import lumien.randomthings.entity.SpectreIlluminatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Places a floating Spectre Illuminator that keeps its chunk fully lit. */
public class SpectreIlluminatorItem extends Item {
    public SpectreIlluminatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide && !SpectreIlluminatorEntity.isChunkIlluminated(pos, level)) {
            ChunkPos chunkPos = new ChunkPos(pos);
            AABB searchBox = new AABB(
                chunkPos.getMinBlockX() - 2, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 2,
                chunkPos.getMaxBlockX() + 2, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 2);
            List<SpectreIlluminatorEntity> existing = level.getEntitiesOfClass(SpectreIlluminatorEntity.class, searchBox);
            if (existing.isEmpty()) {
                SpectreIlluminatorEntity illuminator = new SpectreIlluminatorEntity(
                    level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                level.addFreshEntity(illuminator);
                if (!context.getPlayer().getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return level.isClientSide ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
