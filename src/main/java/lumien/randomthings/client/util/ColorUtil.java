package lumien.randomthings.client.util;

import java.awt.Color;
import com.mojang.blaze3d.systems.RenderSystem;

public class ColorUtil {

    public static Color brighter(Color c1, float amount) {
        return new Color((int) Math.min(255, c1.getRed() + amount), (int) Math.min(255, c1.getGreen() + amount), (int) Math.min(255, c1.getBlue() + amount), c1.getAlpha());
    }

    public static void applyColor(Color c) {
        RenderSystem.setShaderColor(1F / 255 * c.getRed(), 1F / 255 * c.getGreen(), 1F / 255 * c.getBlue(), 1F / 255 * c.getAlpha());
    }

    public static Color interpolate(Color c1, Color c2, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int r = (int)(c1.getRed() * (1f - ratio) + c2.getRed() * ratio);
        int g = (int)(c1.getGreen() * (1f - ratio) + c2.getGreen() * ratio);
        int b = (int)(c1.getBlue() * (1f - ratio) + c2.getBlue() * ratio);
        int a = (int)(c1.getAlpha() * (1f - ratio) + c2.getAlpha() * ratio);
        return new Color(r, g, b, a);
    }
}