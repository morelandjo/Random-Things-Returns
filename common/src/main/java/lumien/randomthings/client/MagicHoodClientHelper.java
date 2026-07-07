package lumien.randomthings.client;

import lumien.randomthings.item.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;

/** Client-side checks for the Magic Hood's stealth effects (used by the render mixins). */
@Environment(EnvType.CLIENT)
public final class MagicHoodClientHelper {

    private MagicHoodClientHelper() {
    }

    public static boolean isWearingMagicHood(Player player) {
        return player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.MAGIC_HOOD.get());
    }
}
