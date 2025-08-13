package lumien.randomthings.client.notifications;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NotificationToast implements Toast {
    // Use the recipe toast sprite which has a lighter background
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/recipe");
    public static final int DISPLAY_TIME = 5000;
    
    private final String title;
    private final String body;
    private final ItemStack icon;

    private long firstDrawTime;
    private boolean hasInitialized = false;

    public NotificationToast(String title, String body, ItemStack icon) {
        this.title = title;
        this.body = body;
        this.icon = icon;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (!hasInitialized) {
            firstDrawTime = timeSinceLastVisible;
            hasInitialized = true;
        }

        // Draw toast background using the same sprite as vanilla advancement toasts
        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());

        // Draw title and body text using colors appropriate for recipe toast
        // Title in purple (0xFF500050 - good contrast on light background)
        guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 7, 0xFF500050, false);
        // Body/description in black (0xFF000000 - readable on light background)
        guiGraphics.drawString(toastComponent.getMinecraft().font, body, 30, 18, 0xFF000000, false);

        // Draw icon using renderFakeItem like vanilla toasts
        if (!icon.isEmpty()) {
            guiGraphics.renderFakeItem(icon, 8, 8);
        }

        return (double)timeSinceLastVisible >= 5000.0 * toastComponent.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }
}