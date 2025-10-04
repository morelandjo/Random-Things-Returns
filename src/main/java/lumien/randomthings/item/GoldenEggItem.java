package lumien.randomthings.item;

import lumien.randomthings.entity.GoldenEggEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GoldenEggItem extends Item {

    public GoldenEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Play egg throw sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
            0.4F / (level.getRandom().nextFloat() * 0.4F + 1.0F));

        if (!level.isClientSide) {
            // Create and spawn the golden egg entity
            GoldenEggEntity goldenEgg = new GoldenEggEntity(level, player);
            goldenEgg.setItem(itemStack);
            goldenEgg.shootFromRotation(player, player.getXRot(), player.getYRot(),
                0.0F, 1.5F, 1.0F);
            level.addFreshEntity(goldenEgg);
        }

        // Update stats and consume item
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
