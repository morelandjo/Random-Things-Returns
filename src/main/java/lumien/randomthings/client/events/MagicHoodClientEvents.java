package lumien.randomthings.client.events;

import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Client-side event handler for Magic Hood effects.
 * Suppresses nameplate rendering and potion particles when the hood is worn.
 */
@EventBusSubscriber(value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class MagicHoodClientEvents {

    /**
     * Suppress nameplate rendering for players wearing the Magic Hood
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        // Only apply to players
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if player is wearing Magic Hood
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(ModItems.MAGIC_HOOD.get())) {
            // Cancel the name tag rendering
            event.setCanRender(TriState.FALSE);
        }
    }

    /**
     * Check if a player is wearing the Magic Hood
     * This method can be called from other rendering code to suppress potion particles
     */
    public static boolean isWearingMagicHood(Player player) {
        if (player == null) {
            return false;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.is(ModItems.MAGIC_HOOD.get());
    }

    /**
     * Check if the client player is wearing the Magic Hood
     */
    public static boolean isClientPlayerWearingMagicHood() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        return isWearingMagicHood(mc.player);
    }
}
