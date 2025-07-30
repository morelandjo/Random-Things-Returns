package lumien.randomthings.util.math;

import java.util.function.Function;
import org.joml.Vector3f;

public class MathUtils {
    public static Function<Float, Vector3f> getSpiralFunction(Vector3f from, Vector3f to, float maxRadius, int rotations, float offset) {
        Vector3f dif = new Vector3f(to).sub(from);
        
        Vector3f axis1 = new Vector3f();
        dif.cross(0, 1, 0, axis1);
        axis1.normalize();
        
        Vector3f axis2 = new Vector3f();
        dif.cross(axis1, axis2);
        axis2.normalize();
        
        return (progress) -> {
            Vector3f between = new Vector3f(from).lerp(to, progress);
            
            double theta = progress * Math.PI * 2 * rotations + offset;
            float radius = (float) (Math.sin(progress * Math.PI) * maxRadius);
            
            float sin = (float) Math.sin(theta) * radius;
            float cos = (float) Math.cos(theta) * radius;
            
            Vector3f sS = new Vector3f(axis1).mul(sin);
            Vector3f sC = new Vector3f(axis2).mul(cos);
            Vector3f circlePoint = new Vector3f(sS).add(sC);
            
            return between.add(circlePoint);
        };
    }
}