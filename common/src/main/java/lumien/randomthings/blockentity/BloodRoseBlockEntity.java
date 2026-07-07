package lumien.randomthings.blockentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Drives the Blood Rose: once matured it hurts nearby creatures (giving players a petal) and slowly
 * spreads to nearby dirt. (The 1.21.1 build also streamed cosmetic blood particles via a custom
 * packet; that purely-visual effect is dropped in this port.)
 */
public class BloodRoseBlockEntity extends BlockEntity {
    private BlockPos targetSpreadPos;
    private int spreadTick;
    private int damageTick;
    private int progress;
    private static final int MAX_PROGRESS = 20;

    public BloodRoseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLOOD_ROSE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BloodRoseBlockEntity blockEntity) {
        blockEntity.tick();
    }

    public void tick() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (progress != MAX_PROGRESS) {
            progress++;
            return;
        }

        damageTick++;
        if (damageTick % 20 == 0) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition).inflate(2, 2, 2));
            for (LivingEntity entity : entities) {
                if (entity instanceof Player player) {
                    if (!player.isCreative() && !player.isSpectator()) {
                        player.hurt(level.damageSources().magic(), 1.0F);
                        player.getInventory().add(new ItemStack(ModItems.BLOOD_ROSE_PETAL.get()));
                    }
                } else {
                    entity.hurt(level.damageSources().magic(), 1.0F);
                }
            }
        }

        spreadTick++;
        if (spreadTick >= 20 * 10) {
            spreadTick = 0;
            if (targetSpreadPos == null) {
                List<BlockPos> valid = new ArrayList<>();
                for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-10, -3, -10), worldPosition.offset(10, 3, 10))) {
                    if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(BlockTags.DIRT)) {
                        valid.add(p.immutable());
                    }
                }
                if (!valid.isEmpty()) {
                    Collections.shuffle(valid);
                    targetSpreadPos = valid.get(0);
                    progress = 0;
                }
            } else {
                progress++;
                if (progress >= MAX_PROGRESS) {
                    level.setBlock(targetSpreadPos, ModBlocks.BLOOD_ROSE.get().defaultBlockState(), 3);
                    targetSpreadPos = null;
                    progress = MAX_PROGRESS;
                }
            }
        }
    }
}
