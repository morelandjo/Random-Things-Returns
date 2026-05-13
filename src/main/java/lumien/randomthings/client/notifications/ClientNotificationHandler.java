package lumien.randomthings.client.notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only entry point for showing notification toasts. Isolating this in its own class
 * keeps the supertype chain ({@code NotificationToast} → {@code Toast}) out of the bytecode
 * that the dedicated server has to verify when it loads the network message handler.
 */
public final class ClientNotificationHandler {
    private ClientNotificationHandler() {}

    public static void show(String title, String description, ItemStack icon) {
        Minecraft.getInstance().getToasts().addToast(new NotificationToast(title, description, icon));
    }
}
