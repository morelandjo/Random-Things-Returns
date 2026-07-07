package lumien.randomthings.item;

import lumien.randomthings.entity.ThrownWeatherEggEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Throwable egg that summons a weather cloud which sets the world's weather. */
public class WeatherEggItem extends Item {

    public enum WeatherType {
        SUN,
        RAIN,
        STORM
    }

    private final WeatherType weatherType;

    public WeatherEggItem(WeatherType weatherType, Properties properties) {
        super(properties);
        this.weatherType = weatherType;
    }

    public WeatherType getWeatherType() {
        return weatherType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.EGG_THROW, SoundSource.PLAYERS,
            0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownWeatherEggEntity thrownEgg = new ThrownWeatherEggEntity(level, player, weatherType);
            thrownEgg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrownEgg);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}
