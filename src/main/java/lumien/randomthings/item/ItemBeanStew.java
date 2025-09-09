package lumien.randomthings.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.food.FoodProperties;

public class ItemBeanStew extends Item {
    
    private static final FoodProperties BEAN_STEW_FOOD = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.6f)
            .build();
    
    public ItemBeanStew() {
        super(new Item.Properties()
                .food(BEAN_STEW_FOOD)
                .stacksTo(1));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(itemStack, level, livingEntity);
        
        if (livingEntity instanceof Player player) {
            // Return empty bowl to player after eating stew
            if (!player.getAbilities().instabuild) {
                if (itemStack.isEmpty()) {
                    return new ItemStack(Items.BOWL);
                } else {
                    // If player has more stew, give them back the bowl
                    if (!player.getInventory().add(new ItemStack(Items.BOWL))) {
                        player.drop(new ItemStack(Items.BOWL), false);
                    }
                }
            }
        }
        
        return result;
    }
}