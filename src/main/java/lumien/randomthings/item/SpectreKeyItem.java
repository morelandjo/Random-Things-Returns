package lumien.randomthings.item;

import lumien.randomthings.handler.spectre.SpectreHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Spectre Key - Grants access to a private room in the Spectre Dimension.
 * Hold right-click for 5 seconds (100 ticks) to teleport.
 */
public class SpectreKeyItem extends Item {

    public SpectreKeyItem(Properties properties) {
        super(properties.stacksTo(1)); // Max stack size of 1
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 100; // 5 seconds (100 ticks)
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW; // Charge-up animation like bow
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
                    // In Spectre Dimension, teleport back
                    handler.teleportPlayerBack(player);
                } else {
                    // Not in Spectre Dimension, teleport to cube
                    handler.teleportPlayerToSpectreCube(player);
                }
            }
        }

        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // Only spawn particles on client side
        if (level.isClientSide && remainingUseDuration < 60) {
            // Spawn increasing particles as charge builds up
            int particleCount = (60 - remainingUseDuration) * 2;

            for (int i = 0; i < particleCount; i++) {
                // Random position around entity
                double x = entity.getX() + (level.random.nextDouble() - 0.5) * 1.8;
                double y = entity.getY() + level.random.nextDouble() * 1.8;
                double z = entity.getZ() + (level.random.nextDouble() - 0.5) * 1.8;

                // Cyan-colored smoke particles (122, 197, 205 in RGB)
                level.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    x, y, z,
                    0.0, 0.05, 0.0
                );
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Show enchantment glint when in Spectre Dimension
        // This requires client-side check
        return false; // TODO: Implement client-side dimension check
    }
}
