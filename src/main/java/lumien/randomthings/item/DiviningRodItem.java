package lumien.randomthings.item;

import java.awt.Color;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DiviningRodItem extends Item {
    String[] detectingTags;
    Color[] colors;

    public DiviningRodItem(Properties properties, Color[] colors, String[] detectingTags) {
        super(properties.stacksTo(1));

        this.detectingTags = detectingTags;
        this.colors = colors;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public String[] getDetectedTags() {
        return detectingTags;
    }

    public int matches(BlockState blockState) {
        // Only check non-air blocks to reduce spam
        if (blockState.isAir()) {
            return -1;
        }
        
        for (int i = 0; i < detectingTags.length; i++) {
            try {
                ResourceLocation tagLocation = ResourceLocation.parse(detectingTags[i]);
                TagKey<Block> tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), tagLocation);
                
                if (blockState.is(tagKey)) {
                    return i;
                }
            } catch (Exception e) {
                // Silently ignore invalid tag formats
            }
        }
        return -1;
    }
}