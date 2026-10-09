package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.markscitygen.lib.Vec2;

public class RoadGenerator {
    /// If the direction vector at the current point is shorter than this length,
    /// the point is considered degenerate and algorithm stops.
    static final double MIN_DIRECTION_LENGTH = 0.03;
    /// Avoids looping in radial tensor fields.
    static final int MAX_STEPS = 10000;

    static final double STEP_SIZE = 0.5;

    final TensorField2 field;
    final ArrayList<Path2> roads = new ArrayList<>();
    final Vec2 bounds;
    final double maxLength;

    private RoadGenerator(Builder builder) {
        this.field = builder.field;
        this.bounds = builder.bounds;
        this.maxLength = builder.maxLength / STEP_SIZE;
    }

    public static class Builder {
        private final TensorField2 field;
        private Vec2 bounds;
        private double maxLength;

        public Builder(TensorField2 field) {
            this.field = field;
        }

        public Builder bounds(Vec2 bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder maxLength(double maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public RoadGenerator build() {
            return new RoadGenerator(this);
        }
    }

    public ArrayList<Path2> getRoads() {
        return roads;
    }

    public void generateRoads(int numRoads) {
        for (int i = 0; i < numRoads; i++) {
            generateRoad();
        }
    }

    private Vec2 getDirectionAt(Vec2 point, boolean isPrimary, Vec2 prevDirection) {
        var tensor = field.getTensorAt(point);
        var direction = isPrimary ? tensor.getPrimaryDirection() : tensor.getSecondaryDirection();
        direction.alignWith(prevDirection);
        return direction;
    }

    public void generateRoad() {
        var path = new Path2();

        var seed = Vec2.randomBounded(bounds);
        path.points.add(seed);

        var isPrimary = Math.random() < 0.5;

        double totalLength = 0;
        Vec2 lastDirection = Vec2.randomUnit();
        for (int steps = 0; steps < MAX_STEPS; steps++) {
            // Runge-Kutta 4th order integration step
            var lastPoint = path.points.getLast();
            var k1 = getDirectionAt(lastPoint, isPrimary, lastDirection);
            var p1 = Vec2.sum(lastPoint, STEP_SIZE / 2, k1);
            var k2 = getDirectionAt(p1, isPrimary, lastDirection);
            var p2 = Vec2.sum(lastPoint, STEP_SIZE / 2, k2);
            var k3 = getDirectionAt(p2, isPrimary, lastDirection);
            var p3 = Vec2.sum(lastPoint, STEP_SIZE, k3);
            var k4 = getDirectionAt(p3, isPrimary, lastDirection);
            var nextPoint =
                    Vec2.sum(
                            lastPoint,
                            STEP_SIZE / 6,
                            k1,
                            STEP_SIZE / 3,
                            k2,
                            STEP_SIZE / 3,
                            k3,
                            STEP_SIZE / 6,
                            k4);

            var direction = lastPoint.directionTo(nextPoint);

            var directionLength = direction.length();
            if (directionLength < MIN_DIRECTION_LENGTH) {
                break;
            }

            if (!nextPoint.isInBounds(bounds)) {
                break;
            }

            path.points.add(nextPoint);

            totalLength += directionLength;
            if (totalLength >= maxLength) {
                break;
            }

            lastDirection = direction;
        }

        roads.add(path);
    }
}
