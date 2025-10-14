package lumien.randomthings.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;

public class SuperLubricentTinctureItem extends Item {

    public SuperLubricentTinctureItem() {
        super(new Item.Properties()
            .rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Give it a subtle enchantment glint
        return false;
    }
}
