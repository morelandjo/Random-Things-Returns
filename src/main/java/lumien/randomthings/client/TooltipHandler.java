package lumien.randomthings.client;

import lumien.randomthings.item.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = "randomthings")
public class TooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // Check if the item is anchored
        if (event.getItemStack().has(ModDataComponents.SPECTRE_ANCHORED.get())) {
            Boolean isAnchored = event.getItemStack().get(ModDataComponents.SPECTRE_ANCHORED.get());

            if (isAnchored != null && isAnchored) {
                // Add "Anchored" tooltip (add at index 1, after the item name)
                event.getToolTip().add(1,
                    Component.translatable("tooltip.randomthings.spectre_anchor.anchored")
                        .withStyle(ChatFormatting.DARK_AQUA)
                );
            }
        }
    }
}
