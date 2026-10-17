package org.markscitygen.lib.tensorfield;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import org.markscitygen.lib.Path2;
import org.markscitygen.lib.Vec2;

public final class RoadGenerator {
    final ArrayList<Path2> roads = new ArrayList<>();
    final RoadDensityChecker majorRoadDensityChecker;
    final RoadDensityChecker minorRoadDensityChecker;

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

        this.majorRoadDensityChecker = new RoadDensityChecker(bounds, crossingDistance);
        this.minorRoadDensityChecker = new RoadDensityChecker(bounds, crossingDistance);
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

    static final class Seed {
        final Vec2 point;
        final boolean isMinorLine;
        final Vec2 growDirection;

        public static Seed randomInBounds(Vec2 bounds) {
            return new Seed(new Vec2(Math.random() * bounds.x, Math.random() * bounds.y));
        }

        Seed(Vec2 point) {
            this(point, false);
        }

        Seed(Vec2 point, boolean isMinorLine) {
            this(point, isMinorLine, Vec2.fromPolarUnit(Math.random() * 2 * Math.PI));
        }

        Seed(Vec2 point, boolean isMinorLine, Vec2 growDirection) {
            this.point = point;
            this.isMinorLine = isMinorLine;
            this.growDirection = growDirection;
        }
    }

    public ArrayList<Path2> getGeneratedRoads() {
        return roads;
    }

    public void generateRoads(int numRoads) {
        var seeds = new ArrayDeque<Seed>();

        for (int i = 0; i < numRoads; i++) {
            var seed = Seed.randomInBounds(bounds);
            seeds.add(seed);
        }

        while (!seeds.isEmpty()) {
            var seed = seeds.pollFirst();
            var road = generateRoad(seed);
            if (road == null) {
                continue;
            }

            addRoadToGenerationContext(road, seed.isMinorLine);
            roads.add(road);
            placeSeeds(road, seed.isMinorLine, seeds);
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

    /// @return null if failed to generate a road (e.g. if the road is too short)
    public Path2 generateRoad(Seed seed) {
        var path = new Path2();
        var totalLength = 0.0;
        var prevPoint = seed.point;
        var prevGrowDirection = seed.growDirection;
        var wentFarFromSeed = false;

        var roadDensityChecker =
                seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker;

        path.add(seed.point);

        for (int steps = 0; steps < maxPoints; steps++) {
            var point = getNextPoint(prevPoint, seed.isMinorLine, prevGrowDirection);

            if (!point.isInBounds(bounds)) {
                break;
            }

            var growDirection = prevPoint.directionTo(point);

            var growLength = growDirection.length();

            if (growLength < minGrowLength) {
                // Hit a degenerate point in the tensor field
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed.point, proximityThreshold)) {
                wentFarFromSeed = true;
            }

            if (wentFarFromSeed && point.isCloseTo(seed.point, proximityThreshold)) {
                path.add(point);
                path.close();
                break;
            }

            if (roadDensityChecker.checkTooDenseNear(point)) {
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
            return null;
        }

        return path;
    }

    private void addRoadToGenerationContext(Path2 road, boolean isMinorLine) {
        var roadDensityChecker = isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker;
        roadDensityChecker.add(road);
    }

    private void placeSeeds(Path2 road, boolean isMinorLine, Collection<Seed> seeds) {
        var length = 0.0;
        for (int i = 0; i < road.segmentCount(); i++) {
            length += road.getSegmentLength(i);

            if (length > crossingDistance) {
                length = 0.0;
                seeds.add(new Seed(road.getPoint(i), !isMinorLine, road.getDirectionAt(i)));
            }
        }
    }
}
