package lumien.randomthings.client.vfx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;

public class VFXHandler {
    public static final VFXHandler INSTANCE = new VFXHandler();
    
    private final List<VisualEffect> currentEffects;
    
    public VFXHandler() {
        this.currentEffects = new ArrayList<>();
    }
    
    public void addEffect(VisualEffect effect) {
        effect.init();
        this.currentEffects.add(effect);
    }
    
    public void tick() {
        Iterator<VisualEffect> iterator = this.currentEffects.iterator();
        
        while (iterator.hasNext()) {
            VisualEffect effect = iterator.next();
            
            if (effect.tick()) {
                iterator.remove();
            }
        }
    }
    
    public void render(PoseStack poseStack, float partialTicks) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        double playerX = cameraPos.x();
        double playerY = cameraPos.y();
        double playerZ = cameraPos.z();

        poseStack.pushPose();
        poseStack.translate(-playerX, -playerY, -playerZ);
        
        this.currentEffects.forEach(effect -> effect.renderInternal(partialTicks));

        poseStack.popPose();
    }
}