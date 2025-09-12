package lumien.randomthings.client.render.magiccircles;

import java.awt.Color;

@FunctionalInterface
public interface IColorFunction {
    Color apply(float progress, int triangle, Color originalColor);

    default IColorFunction after(IColorFunction otherFunction) {
        return (p, t, c) -> {
            return apply(p, t, otherFunction.apply(p, t, c));
        };
    }

    default IColorFunction next(IColorFunction otherFunction) {
        return (p, t, c) -> {
            return otherFunction.apply(p, t, apply(p, t, c));
        };
    }

    default ITriangleFunction tt(float progress) {
        return ITriangleFunction.from(progress, this);
    }
}