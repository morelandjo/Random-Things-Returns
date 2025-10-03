package lumien.randomthings.item;

import lumien.randomthings.handler.EscapeRopeHandler;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class EscapeRopeItem extends Item {

    // Golden particle color for escape rope effect
    private static final DustParticleOptions GOLDEN_DUST = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.0F), 1.0F);

    public EscapeRopeItem() {
        super(new Properties()
            .stacksTo(1)
            .durability(20)); // 21 uses total (0-20 damage)
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 20 * 60; // 60 seconds max use time
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Check if we're in a cave (no sky light, can't see sky, not in air block)
        if (!level.dimensionType().hasSkyLight() ||
            level.canSeeSky(player.blockPosition()) ||
            !level.isEmptyBlock(player.blockPosition())) {
            return InteractionResultHolder.fail(itemStack);
        }

        player.startUsingItem(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            EscapeRopeHandler.getInstance().addTask(serverPlayer);
        }

        return InteractionResultHolder.success(itemStack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide) {
            int totalDuration = getUseDuration(stack, livingEntity);
            int usedDuration = totalDuration - remainingUseDuration;

            // Calculate alpha based on how long we've been using the item
            float alpha = Math.min(1.0f, usedDuration * (1.0f / 60.0f));

            // Create spiral particle effect around the player
            for (int i = 0; i < 7; i++) {
                for (int c = 0; c < 20; c += 10) {
                    double x = Math.sin((usedDuration + i * 20) / (10.0f + c));
                    double z = Math.cos((usedDuration + i * 20) / (10.0f + c));
                    double y = Math.sin((usedDuration + i * 20) / (15.0f + c));

                    // Spawn golden dust particles with calculated alpha
                    level.addParticle(GOLDEN_DUST,
                        livingEntity.getX() + x,
                        livingEntity.getY() + 1 + y,
                        livingEntity.getZ() + z,
                        0, 0, 0);
                }
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Make the item glint when actively being used
        // For now, we'll remove this feature as it requires more complex data component management
        return super.isFoil(stack);
    }
}