package lumien.randomthings.block;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lumien.randomthings.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class SticksBlock extends Block {
    boolean returning;

    public SticksBlock(boolean returning) {
        super(BlockBehaviour.Properties.of()
            .strength(0.5F)
            .sound(SoundType.WOOD)
            .noOcclusion()
            .isViewBlocking((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false));

        this.returning = returning;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 20 * 10);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isOcclusionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
        return false;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);

        if (returning) {
            level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.6f, 1.2f);

            List<Player> playerList = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(50, 50, 50));

            if (!playerList.isEmpty()) {
                Collections.sort(playerList, new Comparator<Player>() {
                    @Override
                    public int compare(Player o1, Player o2) {
                        return o1.blockPosition().distSqr(pos) >= o2.blockPosition().distSqr(pos) ? 1 : -1;
                    }
                });

                Player closest = playerList.get(0);

                if (!closest.isCreative()) {
                    ItemStack itemToReturn = new ItemStack(ModItems.BLOCK_OF_STICKS_RETURNING.get());
                    if (!closest.getInventory().add(itemToReturn)) {
                        // If inventory is full, drop the item at the player's location
                        closest.drop(itemToReturn, false);
                    }
                }
            }
        } else {
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.6f, 1.2f);
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }
}