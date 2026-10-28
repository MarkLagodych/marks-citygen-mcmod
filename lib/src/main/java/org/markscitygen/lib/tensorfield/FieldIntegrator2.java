package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.core.Vec2;

public class FieldIntegrator2 {
    private final TensorField2 field;
    private final double stepSize;

    public FieldIntegrator2(TensorField2 field, double stepSize) {
        this.field = field;
        this.stepSize = stepSize;
    }

    private Vec2 getGrowDirectionAt(Vec2 point, boolean isMinorLine, Vec2 prevGrowDirection) {
        var growDirection = field.getTensorAt(point).getMajorEigenvector();
        if (isMinorLine) growDirection.rotateLeft();
        growDirection.alignWith(prevGrowDirection);
        return growDirection;
    }

    public Vec2 getNextPoint(Vec2 prevPoint, boolean isMinorLine, Vec2 prevGrowDirection) {
        // Runge-Kutta 4th order integration step

        var h = stepSize;
        // TODO Adapt the step size for better precision

        var p1 = prevPoint;
        var k1 = getGrowDirectionAt(p1, isMinorLine, prevGrowDirection);

        var p2 = Vec2.sum(p1, h / 2, k1);
        var k2 = getGrowDirectionAt(p2, isMinorLine, prevGrowDirection);

        var p3 = Vec2.sum(p1, h / 2, k2);
        var k3 = getGrowDirectionAt(p3, isMinorLine, prevGrowDirection);

        var p4 = Vec2.sum(p1, h, k3);
        var k4 = getGrowDirectionAt(p4, isMinorLine, prevGrowDirection);

        return Vec2.sum(p1, h / 6, k1, h / 3, k2, h / 3, k3, h / 6, k4);
    }
}
