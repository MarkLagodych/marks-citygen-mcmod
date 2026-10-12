package org.markscitygen.lib.tensorfield;

import java.util.ArrayList;
import org.markscitygen.lib.Path2;
import org.markscitygen.lib.PointSearchMap2;
import org.markscitygen.lib.Vec2;

public final class RoadGenerator {
    final ArrayList<Path2> roads = new ArrayList<>();
    final PointSearchMap2 majorPointMap;
    final PointSearchMap2 minorPointMap;

    final TensorField2 field;
    final Vec2 bounds;
    final double stepSize;
    final int maxPoints;
    final double crossingDistance;
    final double minRoadLength;
    final double maxRoadLength;
    final double minGrowLength;
    final double proximityThreshold;

    private RoadGenerator(Builder builder) {
        this.field = builder.field;
        this.bounds = builder.bounds;
        this.maxPoints = builder.maxPoints;
        this.stepSize = builder.stepSize;
        this.maxRoadLength = builder.maxRoadLength;
        this.minRoadLength = builder.minRoadLength;
        this.minGrowLength = builder.minGrowLength;
        this.crossingDistance = builder.crossingDistance;
        this.proximityThreshold = builder.proximityThreshold;

        this.majorPointMap = new PointSearchMap2(bounds, crossingDistance);
        this.minorPointMap = new PointSearchMap2(bounds, crossingDistance);
    }

    public static final class Builder {
        private final TensorField2 field;
        private Vec2 bounds = new Vec2(1000, 1000);
        private int maxPoints = 10000;
        private double stepSize = 0.5;
        private double minRoadLength = 10;
        private double maxRoadLength = 1000;
        private double minGrowLength = 0.03;
        private double crossingDistance = 30;
        private double proximityThreshold = 1.0;

        public Builder(TensorField2 field) {
            this.field = field;
        }

        /// The input bouding box size
        public Builder bounds(Vec2 bounds) {
            this.bounds = bounds;
            return this;
        }

        public Builder maxPoints(int maxPoints) {
            this.maxPoints = maxPoints;
            return this;
        }

        /// Road growth step size
        public Builder stepSize(double stepSize) {
            this.stepSize = stepSize;
            return this;
        }

        public Builder minRoadLength(double minRoadLength) {
            this.minRoadLength = minRoadLength;
            return this;
        }

        public Builder maxRoadLength(double maxRoadLength) {
            this.maxRoadLength = maxRoadLength;
            return this;
        }

        /// If the grow direction vector at the current point is shorter than this length,
        /// the point is considered degenerate and road growing stops.
        public Builder minGrowLength(double minGrowLength) {
            this.minGrowLength = minGrowLength;
            return this;
        }

        /// Preferred distance between crossings
        public Builder crossingDistance(double crossingDistance) {
            this.crossingDistance = crossingDistance;
            return this;
        }

        /// Tolerance for distance checking: if `|p1-p2| <= proximityThreshold`,
        /// then p1 and p2 are considered to be at the same location.
        /// This is used for intersection detection.
        public Builder proximityThreshold(double proximityThreshold) {
            this.proximityThreshold = proximityThreshold;
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

    private Vec2 getGrowDirectionAt(Vec2 point, boolean isMinorLine, Vec2 prevGrowDirection) {
        var growDirection = field.getTensorAt(point).getMajorEigenvector();
        if (isMinorLine) growDirection.rotatePiHalf();
        growDirection.alignWith(prevGrowDirection);
        return growDirection;
    }

    private Vec2 getNextPoint(Vec2 prevPoint, boolean isMinorLine, Vec2 prevGrowDirection) {
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

    public void generateRoad() {
        var seed = new Vec2(Math.random() * bounds.x, Math.random() * bounds.y);

        var path = new Path2();
        var totalLength = 0.0;
        var isMinorLine = Math.random() < 0.5;
        var prevPoint = seed;
        var prevGrowDirection = Vec2.fromPolarUnit(Math.random() * 2 * Math.PI);
        var wentFarFromSeed = false;

        var pointMap = isMinorLine ? minorPointMap : majorPointMap;

        path.add(seed);

        for (int steps = 0; steps < maxPoints; steps++) {
            var point = getNextPoint(prevPoint, isMinorLine, prevGrowDirection);

            if (!point.isInBounds(bounds)) {
                break;
            }

            var growDirection = prevPoint.directionTo(point);

            var growLength = growDirection.length();

            if (growLength < minGrowLength) {
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed, proximityThreshold)) {
                wentFarFromSeed = true;
            }

            if (wentFarFromSeed && point.isCloseTo(seed, proximityThreshold)) {
                path.add(point);
                path.add(seed);
                break;
            }

            if (pointMap.anyPointsNear(point)) {
                // TODO look ahead to join another road
                break;
            }

            if (totalLength + growLength >= maxRoadLength) {
                // TODO look ahead to join another road
                break;
            }

            path.add(point);

            prevPoint = point;
            prevGrowDirection = growDirection;
            totalLength += growLength;
        }

        if (totalLength < minRoadLength) {
            return;
        }

        for (int i = 0; i < path.size(); i++) {
            pointMap.add(path.get(i));
        }

        roads.add(path);
    }
}
