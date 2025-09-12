package lumien.randomthings.client.render.magiccircles;

import java.awt.Color;
import java.util.function.Function;

@FunctionalInterface
public interface ITriangleFunction {
    Color apply(int triangle);
    
    static ITriangleFunction from(float progress, IColorFunction colorFunction) {
        return (i) -> {
            return colorFunction.apply(progress, i, Color.WHITE);
        };
    }
    
    default ITriangleFunction darker() {
        return (i) -> {
            return apply(i).darker();
        };
    }
    
    default ITriangleFunction modColor(Function<Color, Color> other) {
        return (i) -> {
            return other.apply(apply(i));
        };
    }
}