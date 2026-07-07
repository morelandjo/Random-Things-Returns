package lumien.randomthings.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.Color;

/** Held in hand, highlights nearby ores matching its configured block tag(s). */
public class DiviningRodItem extends Item {
    private final String[] detectingTags;
    private final Color[] colors;

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

    /** Returns the index of the matching tag (for the colour), or -1 if the state matches none. */
    public int matches(BlockState blockState) {
        if (blockState.isAir()) {
            return -1;
        }
        for (int i = 0; i < detectingTags.length; i++) {
            try {
                TagKey<Block> tagKey = TagKey.create(BuiltInRegistries.BLOCK.key(), new ResourceLocation(detectingTags[i]));
                if (blockState.is(tagKey)) {
                    return i;
                }
            } catch (Exception ignored) {
                // skip malformed tag entries
            }
        }
        return -1;
    }
}
