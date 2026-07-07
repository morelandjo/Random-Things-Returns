package lumien.randomthings.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.utils.value.IntValue;
import lumien.randomthings.enchantment.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cross-loader game-event handlers (Architectury events). Currently hosts the Magnetic enchantment's
 * drops-to-inventory behaviour. (NeoForge's 1.21 {@code BlockDropsEvent} has no 1.20.1 equivalent, so
 * we reimplement the harvest on {@link BlockEvent#BREAK}.)
 */
public final class RTEvents {

    private RTEvents() {
    }

    public static void register() {
        BlockEvent.BREAK.register(RTEvents::onBlockBreak);
        EntityEvent.LIVING_CHECK_SPAWN.register(RTEvents::onCheckSpawn);
    }

    /** Peace candles: block monster spawns in chunks near an active candle. */
    private static EventResult onCheckSpawn(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.level.LevelAccessor world,
                                            double x, double y, double z, net.minecraft.world.entity.MobSpawnType type,
                                            @Nullable net.minecraft.world.level.BaseSpawner spawner) {
        if (entity instanceof net.minecraft.world.entity.monster.Monster
            && lumien.randomthings.blockentity.PeaceCandleBlockEntity.isPeacefulChunk(
                net.minecraft.util.Mth.floor(x) >> 4, net.minecraft.util.Mth.floor(z) >> 4)) {
            return EventResult.interruptFalse();
        }
        return EventResult.pass();
    }

    private static EventResult onBlockBreak(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        if (level.isClientSide() || player == null || player.isCreative()) {
            return EventResult.pass();
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return EventResult.pass();
        }
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty() || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MAGNETIC.get(), tool) <= 0) {
            return EventResult.pass();
        }

        BlockEntity be = serverLevel.getBlockEntity(pos);
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, be, player, tool);

        // Break the block without natural drops (handles break effects + block-entity removal),
        // then deposit the computed drops straight into the player's inventory.
        serverLevel.destroyBlock(pos, false, player);
        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop) && !drop.isEmpty()) {
                player.drop(drop, false);
            }
        }
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));

        return EventResult.interruptFalse(); // we performed the break ourselves
    }
}
