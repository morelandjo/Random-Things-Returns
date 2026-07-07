package lumien.randomthings.client.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.item.DiviningRodItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Scans blocks around the player while a divining rod is held and highlights matching ores with
 * translucent wireframe cubes. Rendered from {@code LevelRendererMixin} at the end of the level
 * render pass (Architectury has no cross-loader level-render event).
 */
@Environment(EnvType.CLIENT)
public class DiviningRodRenderer {
    public static DiviningRodRenderer INSTANCE;

    private final List<Indicator> indicators;
    private int modX, modY, modZ;
    private DiviningRodItem lastRodType = null;

    public DiviningRodRenderer() {
        indicators = new ArrayList<>();
    }

    /** Entry point called from the LevelRenderer mixin. */
    public static void renderOverlays(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        get().render(poseStack, bufferSource);
        bufferSource.endBatch();
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource) {
        if (indicators.isEmpty()) {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        for (Indicator indicator : indicators) {
            float size = 0.5F;
            Color c = indicator.color;
            int alpha = 180;

            // Distinct high-contrast colors for the common ore types
            int red, green, blue;
            if (c.getRed() == 20 && c.getGreen() == 20 && c.getBlue() == 20) {
                red = 0; green = 0; blue = 0;            // coal
            } else if (c.getRed() == 211 && c.getGreen() == 180 && c.getBlue() == 159) {
                red = 200; green = 200; blue = 200;      // iron
            } else if (c.getRed() == 246 && c.getGreen() == 233 && c.getBlue() == 80) {
                red = 255; green = 215; blue = 0;        // gold
            } else if (c.getRed() == 87 && c.getGreen() == 221 && c.getBlue() == 229) {
                red = 135; green = 206; blue = 250;      // diamond
            } else {
                red = Math.min(255, Math.max(80, c.getRed() * 2));
                green = Math.min(255, Math.max(80, c.getGreen() * 2));
                blue = Math.min(255, Math.max(80, c.getBlue() * 2));
            }

            float bx = (float) (indicator.target.getX() + 0.5 - size / 2);
            float by = (float) (indicator.target.getY() + 0.5 - size / 2);
            float bz = (float) (indicator.target.getZ() + 0.5 - size / 2);
            RenderUtils.drawCube(poseStack, bufferSource, bx, by, bz, size, red, green, blue, alpha);
            RenderUtils.drawWireframeCube(poseStack, bufferSource, bx, by, bz, size, red, green, blue, 255);
        }

        poseStack.popPose();
    }

    public void tick() {
        Iterator<Indicator> indicatorIterator = indicators.iterator();
        while (indicatorIterator.hasNext()) {
            Indicator i = indicatorIterator.next();
            i.duration--;
            if (i.duration == 0) {
                indicatorIterator.remove();
            }
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Level level = player.level();
        if (level == null) {
            return;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        ItemStack rod = ItemStack.EMPTY;
        if (!main.isEmpty() && main.getItem() instanceof DiviningRodItem) {
            rod = main;
        } else if (!off.isEmpty() && off.getItem() instanceof DiviningRodItem) {
            rod = off;
        }

        if (rod.isEmpty()) {
            if (lastRodType != null) {
                indicators.clear();
                lastRodType = null;
            }
            return;
        }

        DiviningRodItem type = (DiviningRodItem) rod.getItem();
        if (lastRodType != null && lastRodType != type) {
            indicators.clear();
        }
        lastRodType = type;

        BlockPos playerPos = player.blockPosition();
        for (int i = 0; i < 60; i++) {
            modX++;
            if (modX == 6) {
                modX = -5;
                modZ++;
                if (modZ == 6) {
                    modZ = -5;
                    modY++;
                    if (modY == 6) {
                        modY = -5;
                    }
                }
            }

            BlockPos target = playerPos.offset(modX, modY, modZ);
            if (level.isLoaded(target)) {
                BlockState blockState = level.getBlockState(target);
                int matchIndex = type.matches(blockState);
                if (matchIndex != -1) {
                    indicators.add(new Indicator(target, 160, type.getColor(matchIndex), type));
                }
            }
        }
    }

    private static class Indicator {
        BlockPos target;
        int duration;
        Color color;
        DiviningRodItem type;

        public Indicator(BlockPos target, int duration, Color color, DiviningRodItem type) {
            this.target = target;
            this.duration = duration;
            this.color = color;
            this.type = type;
        }
    }

    public static DiviningRodRenderer get() {
        if (INSTANCE == null) {
            INSTANCE = new DiviningRodRenderer();
        }
        return INSTANCE;
    }
}
