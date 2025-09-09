package lumien.randomthings.item;

import lumien.randomthings.client.renderer.PlantChestItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

public class PlantChestItem extends BlockItem {
    
    public PlantChestItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(PlantChestItemRenderer.INSTANCE);
    }
}