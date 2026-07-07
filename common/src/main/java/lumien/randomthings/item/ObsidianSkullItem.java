package lumien.randomthings.item;

import net.minecraft.world.item.Item;

/**
 * Obsidian Skull — while carried, gives a strong chance to negate (non-lava) fire damage.
 * Protection logic lives in {@code LavaCharmHandler}'s {@code LIVING_HURT} listener.
 */
public class ObsidianSkullItem extends Item {
    public ObsidianSkullItem(Properties properties) {
        super(properties.stacksTo(1));
    }
}
