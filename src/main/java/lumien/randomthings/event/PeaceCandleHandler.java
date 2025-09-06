package lumien.randomthings.event;

import lumien.randomthings.blockentity.PeaceCandleBlockEntity;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

// @EventBusSubscriber  // Disabled until we implement spawn prevention
public class PeaceCandleHandler {
    
    /**
     * TODO: Implement mob spawn prevention when we find the correct event API.
     * For now, the peace candle will just be decorative with flame particles.
     * The spawn prevention system can be added in a future update.
     */
    
    // @SubscribeEvent(priority = EventPriority.HIGH)
    // public static void onMobSpawnCheck(MobSpawnEvent.SpawnPlacementCheck event) {
    //     // Placeholder for future spawn prevention implementation
    // }
}