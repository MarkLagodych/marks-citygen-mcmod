package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.markscitygen.lib.OutVec2;
import org.markscitygen.lib.Vec2Math;

public final class RoadGenerator {
    final ArrayList<Path2> roads = new ArrayList<>();

    final TensorField2 field;
    final double width;
    final double height;
    final double maxLength;
    final double stepSize;
    final int maxSteps;
    final double minGrowOffset;

    private RoadGenerator(Builder builder) {
        this.field = builder.field;
        this.width = builder.width;
        this.height = builder.height;
        this.maxLength = builder.maxLength;
        this.minGrowOffset = builder.minGrowOffset;
        this.maxSteps = builder.maxSteps;
        this.stepSize = builder.stepSize;
    }

    public static final class Builder {
        private final TensorField2 field;
        private double width = 1000;
        private double height = 1000;
        private double maxLength = 1000;
        private double minGrowOffset = 0.03;
        private int maxSteps = 10000;
        private double stepSize = 0.5;

        public Builder(TensorField2 field) {
            this.field = field;
        }

        /// The input bouding box size
        public Builder bounds(double width, double height) {
            this.width = width;
            this.height = height;
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

    private void getDirectionAt(
            boolean isSecondary,
            double x,
            double y,
            double lastDirectionX,
            double lastDirectionY,
            OutVec2 outDirection) {

        // Use outDirection as a temporary variable to store the tensor
        field.getTensorAt(x, y, outDirection);
        STTensor2Math.getMajorEigenvector(outDirection.x, outDirection.y, outDirection);

        var dirX = outDirection.x;
        var dirY = outDirection.y;

        if (isSecondary) {
            // Rotate by PI/2
            var temp = dirX;
            dirX = -dirY;
            dirY = temp;
        }

        if (!Vec2Math.isAligned(dirX, dirY, lastDirectionX, lastDirectionY)) {
            dirX = -dirX;
            dirY = -dirY;
        }

        outDirection.x = dirX;
        outDirection.y = dirY;
    }

    public void generateRoad() {
        var path = new Path2();

        path.add(Math.random() * width, Math.random() * height);

        var isSecondary = Math.random() < 0.5;

        double totalLength = 0;

        var randAngle = Math.random() * 2 * Math.PI;
        var lastDirectionX = Math.cos(randAngle);
        var lastDirectionY = Math.sin(randAngle);

        var dir = new OutVec2();

        for (int steps = 0; steps < maxSteps; steps++) {
            // Runge-Kutta 4th order integration step

            var lastPointX = path.getX(path.size() - 1);
            var lastPointY = path.getY(path.size() - 1);

            var nextPointX = lastPointX;
            var nextPointY = lastPointY;

            getDirectionAt(
                    isSecondary, lastPointX, lastPointY, lastDirectionX, lastDirectionY, dir);

            nextPointX += stepSize / 6 * dir.x;
            nextPointY += stepSize / 6 * dir.y;

            var p1X = lastPointX + stepSize / 2 * dir.x;
            var p1Y = lastPointY + stepSize / 2 * dir.y;

            getDirectionAt(isSecondary, p1X, p1Y, lastDirectionX, lastDirectionY, dir);

            nextPointX += stepSize / 3 * dir.x;
            nextPointY += stepSize / 3 * dir.y;

            var p2X = lastPointX + stepSize / 2 * dir.x;
            var p2Y = lastPointY + stepSize / 2 * dir.y;

            getDirectionAt(isSecondary, p2X, p2Y, lastDirectionX, lastDirectionY, dir);

            nextPointX += stepSize / 3 * dir.x;
            nextPointY += stepSize / 3 * dir.y;

            var p3X = lastPointX + stepSize * dir.x;
            var p3Y = lastPointY + stepSize * dir.y;

            getDirectionAt(isSecondary, p3X, p3Y, lastDirectionX, lastDirectionY, dir);

            nextPointX += stepSize / 6 * dir.x;
            nextPointY += stepSize / 6 * dir.y;

            var dirX = nextPointX - lastPointX;
            var dirY = nextPointY - lastPointY;

            var directionLength = Vec2Math.length(dirX, dirY);
            if (directionLength < minGrowOffset) {
                break;
            }

            if (!Vec2Math.isInBounds(nextPointX, nextPointY, width, height)) {
                break;
            }

            path.add(nextPointX, nextPointY);

            totalLength += directionLength;
            if (totalLength >= maxLength) {
                break;
            }

            lastDirectionX = dirX;
            lastDirectionY = dirY;
        }

        roads.add(path);
    }
}
