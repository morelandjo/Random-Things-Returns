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

/** Channel while trapped underground to be pulled up to the surface. */
public class EscapeRopeItem extends Item {
    private static final DustParticleOptions GOLDEN_DUST = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 0.0F), 1.0F);

    public EscapeRopeItem() {
        super(new Properties().stacksTo(1).durability(20));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20 * 60;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.dimensionType().hasSkyLight() || level.canSeeSky(player.blockPosition()) || !level.isEmptyBlock(player.blockPosition())) {
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
            int usedDuration = getUseDuration(stack) - remainingUseDuration;
            for (int i = 0; i < 7; i++) {
                for (int c = 0; c < 20; c += 10) {
                    double x = Math.sin((usedDuration + i * 20) / (10.0f + c));
                    double z = Math.cos((usedDuration + i * 20) / (10.0f + c));
                    double y = Math.sin((usedDuration + i * 20) / (15.0f + c));
                    level.addParticle(GOLDEN_DUST, livingEntity.getX() + x, livingEntity.getY() + 1 + y, livingEntity.getZ() + z, 0, 0, 0);
                }
            }
        }
    }
}
