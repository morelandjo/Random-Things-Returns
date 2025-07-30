package lumien.randomthings.client.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import lumien.randomthings.item.DiviningRodItem;
import lumien.randomthings.client.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DiviningRodRenderer {
    public static DiviningRodRenderer INSTANCE;

    private final List<Indicator> indicators;
    private int modX, modY, modZ;
    private int tickCounter = 0;
    private DiviningRodItem lastRodType = null;

    public DiviningRodRenderer() {
        indicators = new ArrayList<>();
    }

    public boolean shouldGlow(DiviningRodItem rodType) {
        return !indicators.isEmpty() && (indicators.stream().anyMatch((i) -> i.type == rodType));
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks) {
        if (indicators.isEmpty()) {
            return;
        }
        
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        double playerX = cameraPos.x();
        double playerY = cameraPos.y();
        double playerZ = cameraPos.z();

        poseStack.pushPose();
        poseStack.translate(-playerX, -playerY, -playerZ);
        
        for (Indicator indicator : indicators) {
            float size = 0.5F; // Constant size - no pulsing animation
            Color c = indicator.color;
            
            // Use the actual ore colors but make them more visible
            int alpha = 180; // Slightly transparent so you can see multiple overlapping cubes
            
            // Use distinct colors for each ore type
            int red, green, blue;
            if (c.getRed() == 20 && c.getGreen() == 20 && c.getBlue() == 20) {
                // Coal - pure black for maximum contrast with stone
                red = 0; green = 0; blue = 0;
            } else if (c.getRed() == 211 && c.getGreen() == 180 && c.getBlue() == 159) {
                // Iron - metallic silver/gray
                red = 200; green = 200; blue = 200;
            } else if (c.getRed() == 246 && c.getGreen() == 233 && c.getBlue() == 80) {
                // Gold - bright gold
                red = 255; green = 215; blue = 0;
            } else if (c.getRed() == 87 && c.getGreen() == 221 && c.getBlue() == 229) {
                // Diamond - light cyan/blue
                red = 135; green = 206; blue = 250;
            } else {
                // Other ores - enhance brightness while keeping identity
                red = Math.min(255, Math.max(80, c.getRed() * 2));
                green = Math.min(255, Math.max(80, c.getGreen() * 2));
                blue = Math.min(255, Math.max(80, c.getBlue() * 2));
            }
            
            RenderUtils.drawCube(poseStack, bufferSource, 
                (float) (indicator.target.getX() + 0.5 - size / 2), 
                (float) (indicator.target.getY() + 0.5 - size / 2), 
                (float) (indicator.target.getZ() + 0.5 - size / 2), 
                size, red, green, blue, alpha);
                
            // Use the same enhanced colors for wireframe
            RenderUtils.drawWireframeCube(poseStack, bufferSource,
                (float) (indicator.target.getX() + 0.5 - size / 2), 
                (float) (indicator.target.getY() + 0.5 - size / 2), 
                (float) (indicator.target.getZ() + 0.5 - size / 2), 
                size, red, green, blue, 255); // Full opacity wireframe
        }

        poseStack.popPose();
    }

    public void tick() {
        tickCounter++;
        
        Iterator<Indicator> indicatorIterator = indicators.iterator();

        while (indicatorIterator.hasNext()) {
            Indicator i = indicatorIterator.next();
            i.duration--;

            if (i.duration == 0) {
                indicatorIterator.remove();
            }
        }

        Player player = Minecraft.getInstance().player;

        if (player != null) {
            Level level = player.level();

            if (level != null) {
                ItemStack main = player.getMainHandItem();
                ItemStack off = player.getOffhandItem();

                ItemStack rod = ItemStack.EMPTY;
                DiviningRodItem type;

                if (!main.isEmpty() && main.getItem() instanceof DiviningRodItem) {
                    rod = main;
                } else if (!off.isEmpty() && off.getItem() instanceof DiviningRodItem) {
                    rod = off;
                }

                if (!rod.isEmpty()) {
                    type = (DiviningRodItem) rod.getItem();
                    
                    // Clear indicators only when switching to a different rod type
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
                        BlockState blockState = level.getBlockState(target);

                        if (level.isLoaded(target)) {
                            int matchIndex = type.matches(blockState);
                            if (matchIndex != -1) {
                                Indicator indicator = new Indicator(target, 160, type.getColor(matchIndex), type);
                                indicators.add(indicator);
                            }
                        }
                    }
                } else {
                    // Rod left hand - clear indicators and reset tracking
                    if (lastRodType != null) {
                        indicators.clear();
                        lastRodType = null;
                    }
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