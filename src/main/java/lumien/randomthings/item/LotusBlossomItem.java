package lumien.randomthings.item;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LotusBlossomItem extends Item {
    
    private static final FoodProperties FOOD_PROPERTIES = new FoodProperties.Builder()
        .nutrition(2)
        .saturationModifier(0.1F)
        .build();

    public LotusBlossomItem() {
        super(new Item.Properties()
            .food(FOOD_PROPERTIES));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        
        if (livingEntity instanceof Player player && !level.isClientSide) {
            // Give experience like the original implementation
            int experience = 3 + level.random.nextInt(5) + level.random.nextInt(5);
            
            while (experience > 0) {
                int orbValue = ExperienceOrb.getExperienceValue(experience);
                experience -= orbValue;
                level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY(), player.getZ(), orbValue));
            }
        }
        
        return result;
    }
}