package lumien.randomthings.item;

import lumien.randomthings.handler.spectre.SpectreHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Grants access to a private room in the Spectre Dimension.
 * Hold right-click for 5 seconds to teleport there (or back).
 */
public class SpectreKeyItem extends Item {

    public SpectreKeyItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 100; // 5 seconds
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            SpectreHandler handler = SpectreHandler.getInstance(player.getServer());

            if (handler != null) {
                if (SpectreHandler.isSpectreDimension(level)) {
                    handler.teleportPlayerBack(player);
                } else {
                    handler.teleportPlayerToSpectreCube(player);
                }
            }
        }

        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide && remainingUseDuration < 60) {
            int particleCount = (60 - remainingUseDuration) * 2;

            for (int i = 0; i < particleCount; i++) {
                double x = entity.getX() + (level.random.nextDouble() - 0.5) * 1.8;
                double y = entity.getY() + level.random.nextDouble() * 1.8;
                double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 1.8;

                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.05, 0.0);
            }
        }
    }
}
