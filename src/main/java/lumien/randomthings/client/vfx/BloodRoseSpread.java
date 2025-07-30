package lumien.randomthings.client.vfx;

import java.util.function.Function;

import org.joml.Vector3f;

import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.util.math.MathUtils;
import net.minecraft.network.FriendlyByteBuf;

public class BloodRoseSpread extends VisualEffect {
    private Vector3f origin;
    private Vector3f destination;
    private Function<Float, Vector3f>[] functions;

    public BloodRoseSpread() {
        super(100);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init() {
        functions = new Function[8];
        
        for (int i = 0; i < 8; i++) {
            functions[i] = MathUtils.getSpiralFunction(origin, destination, 0.05f, 3, 
                (float) (i * Math.PI / 4F + Math.random() * 0.5F - 0.25F));
        }
    }

    @Override
    public void readData(FriendlyByteBuf buffer) {
        this.origin = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        this.destination = new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void render(float time) {
        // Note: Rendering disabled until proper render event context is available
        /*
        for (int i = 0; i < 8; i++) {
            RenderUtils.drawFunctionLinePart(functions[i], 1f, time / 100F, poseStack, bufferSource);
        }
        */
    }
}