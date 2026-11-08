package org.markscitygen.lib.tensorfield;

import org.markscitygen.lib.core.Vec2;

public final class FieldIntegrator2 {
    final TensorField2 field;

    final boolean useMinorDirection;

    final double minStepSize;
    final double maxStepSize;
    final double tolerance;

    double stepSize;

    Vec2 prevPoint;
    Vec2 prevDirection;

    Vec2 point;
    Vec2 direction;

    public FieldIntegrator2(
            TensorField2 field,
            boolean useMinorDirection,
            double minStepSize,
            double maxStepSize,
            double tolerance,
            Vec2 prevPoint,
            Vec2 prevDirection) {
        this.field = field;
        this.useMinorDirection = useMinorDirection;
        this.minStepSize = minStepSize;
        this.maxStepSize = maxStepSize;
        this.tolerance = tolerance;
        this.point = prevPoint;
        this.direction = prevDirection;
        this.stepSize = maxStepSize;
    }

    public Vec2 nextPoint() {
        return point;
    }

    public Vec2 nextDirection() {
        return direction;
    }

    Vec2 directionAt(Vec2 point) {
        var dir = field.getTensorAt(point).getMajorEigenvector();

        // The minor eigenvector is perpendicular to the major eigenvector
        if (useMinorDirection) dir.rotatePiHalfCW();

        dir.alignWith(prevDirection);

        return dir;
    }

    private static final double K21 = 1. / 4;

    private static final double K31 = 3. / 32;
    private static final double K32 = 9. / 32;

    private static final double K41 = 1932. / 2197;
    private static final double K42 = -7200. / 2197;
    private static final double K43 = 7296. / 2197;

    private static final double K51 = 439. / 216;
    private static final double K52 = -8;
    private static final double K53 = 3680. / 513;
    private static final double K54 = -845. / 4104;

    private static final double K61 = -8. / 27;
    private static final double K62 = 2;
    private static final double K63 = -3544. / 2565;
    private static final double K64 = 1859. / 4104;
    private static final double K65 = -11. / 40;

    private static final double P1 = 25. / 216;
    private static final double P3 = 1408. / 2565;
    private static final double P4 = 2197. / 4104;
    private static final double P5 = -1. / 5;

    private static final double E1 = 1. / 360;
    private static final double E3 = -128. / 4275;
    private static final double E4 = -2197. / 75240;
    private static final double E5 = 1. / 50;
    private static final double E6 = 2. / 55;

    private static final double STEP_FACTOR = 0.84;

    public void calculateNextPoint() {
        prevPoint = point;
        prevDirection = direction;

        // Adaptive Runge-Kutta-Fehlberg 4/5 integration step

        for (; ; ) {
            var k1 = directionAt(prevPoint);
            k1.mul(stepSize);
            var k2 = directionAt(Vec2.sum(prevPoint, K21, k1));
            k2.mul(stepSize);
            var k3 = directionAt(Vec2.sum(prevPoint, K31, k1, K32, k2));
            k3.mul(stepSize);
            var k4 = directionAt(Vec2.sum(prevPoint, K41, k1, K42, k2, K43, k3));
            k4.mul(stepSize);
            var k5 = directionAt(Vec2.sum(prevPoint, K51, k1, K52, k2, K53, k3, K54, k4));
            k5.mul(stepSize);
            var k6 = directionAt(Vec2.sum(prevPoint, K61, k1, K62, k2, K63, k3, K64, k4, K65, k5));
            k6.mul(stepSize);

            var err = Vec2.sum(E1, k1, E3, k3, E4, k4, E5, k5, E6, k6).length() / stepSize;

            var newStep = STEP_FACTOR * stepSize * Math.sqrt(Math.sqrt(tolerance / err));
            stepSize = Math.clamp(newStep, minStepSize, maxStepSize);

            if (err > tolerance && stepSize != minStepSize) {
                continue;
            }

            point = Vec2.sum(prevPoint, P1, k1, P3, k3, P4, k4, P5, k5);

            direction = prevPoint.directionTo(point);

            return;
        }
    }
}
