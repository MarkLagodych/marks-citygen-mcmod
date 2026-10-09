package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.markscitygen.lib.Vec2;

public final class RoadGenerator {
    final ArrayList<Path2> roads = new ArrayList<>();

    final TensorField2 field;
    final Vec2 bounds;
    final double maxLength;
    final double stepSize;
    final int maxSteps;
    final double minGrowOffset;

    private RoadGenerator(Builder builder) {
        this.field = builder.field;
        this.bounds = builder.bounds;
        this.maxLength = builder.maxLength;
        this.minGrowOffset = builder.minGrowOffset;
        this.maxSteps = builder.maxSteps;
        this.stepSize = builder.stepSize;
    }

    public static final class Builder {
        private final TensorField2 field;
        private Vec2 bounds = new Vec2(1000, 1000);
        private double maxLength = 1000;
        private double minGrowOffset = 0.03;
        private int maxSteps = 10000;
        private double stepSize = 0.5;

        public Builder(TensorField2 field) {
            this.field = field;
        }

        /// The input bouding box size
        public Builder bounds(Vec2 bounds) {
            this.bounds = bounds;
            return this;
        }

        /// Maximum road length
        public Builder maxLength(double maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        /// If the direction vector at the current point is shorter than this length,
        /// the point is considered degenerate and algorithm stops.
        public Builder minGrowOffset(double minGrowOffset) {
            this.minGrowOffset = minGrowOffset;
            return this;
        }

        /// Maximum road growth step count. Avoids looping in radial tensor fields.
        public Builder maxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        /// Road growth step size
        public Builder stepSize(double stepSize) {
            this.stepSize = stepSize;
            return this;
        }

        public RoadGenerator build() {
            return new RoadGenerator(this);
        }
    }

    public ArrayList<Path2> getGeneratedRoads() {
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
        path.add(seed);

        var isPrimary = Math.random() < 0.5;

        double totalLength = 0;
        Vec2 lastDirection = Vec2.randomUnit();
        for (int steps = 0; steps < maxSteps; steps++) {
            // Runge-Kutta 4th order integration step
            var lastPoint = path.getLast();
            var k1 = getDirectionAt(lastPoint, isPrimary, lastDirection);
            var p1 = Vec2.sum(lastPoint, stepSize / 2, k1);
            var k2 = getDirectionAt(p1, isPrimary, lastDirection);
            var p2 = Vec2.sum(lastPoint, stepSize / 2, k2);
            var k3 = getDirectionAt(p2, isPrimary, lastDirection);
            var p3 = Vec2.sum(lastPoint, stepSize, k3);
            var k4 = getDirectionAt(p3, isPrimary, lastDirection);
            var nextPoint =
                    Vec2.sum(
                            lastPoint,
                            stepSize / 6,
                            k1,
                            stepSize / 3,
                            k2,
                            stepSize / 3,
                            k3,
                            stepSize / 6,
                            k4);

            var direction = lastPoint.directionTo(nextPoint);

            var directionLength = direction.length();
            if (directionLength < minGrowOffset) {
                break;
            }

            if (!nextPoint.isInBounds(bounds)) {
                break;
            }

            path.add(nextPoint);

            totalLength += directionLength;
            if (totalLength >= maxLength) {
                break;
            }

            lastDirection = direction;
        }

        roads.add(path);
    }
}
