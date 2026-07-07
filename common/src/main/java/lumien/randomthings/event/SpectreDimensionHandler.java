package lumien.randomthings.event;

import dev.architectury.event.events.common.TickEvent;
import lumien.randomthings.handler.spectre.SpectreHandler;
import net.minecraft.server.level.ServerPlayer;

/** Spectre Dimension security: keeps non-creative players inside their own cube. */
public final class SpectreDimensionHandler {

    private SpectreDimensionHandler() {
    }

    public static void register() {
        TickEvent.PLAYER_POST.register(player -> {
            if (!player.level().isClientSide
                && player instanceof ServerPlayer serverPlayer
                && SpectreHandler.isSpectreDimension(player.level())) {
                SpectreHandler handler = SpectreHandler.getInstance(serverPlayer.getServer());
                if (handler != null) {
                    handler.checkPosition(serverPlayer);
                }
            }
        });
    }
}
