package lumien.randomthings.client.vfx;

import java.util.function.Function;

import org.joml.Vector3f;

import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.util.math.MathUtils;
import net.minecraft.network.FriendlyByteBuf;

public class BloodRoseDamage extends VisualEffect {
    private Vector3f origin;
    private Vector3f destination;

    public BloodRoseDamage() {
        super(60);
    }

    @Override
    public void readData(FriendlyByteBuf buffer) {
        this.origin = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.destination = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void render(float time) {
        Function<Float, Vector3f> function = MathUtils.getSpiralFunction(origin, destination, 0.1f, 2, 0);
        
        // Note: Rendering disabled until proper render event context is available
        // RenderUtils.drawFunctionLinePart(function, 0.3f, time / 60F, poseStack, bufferSource);
    }
}