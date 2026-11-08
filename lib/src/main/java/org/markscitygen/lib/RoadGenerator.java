package org.markscitygen.lib;

import java.util.ArrayList;
import java.util.PriorityQueue;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;
import org.markscitygen.lib.tensorfield.TensorField2;

public final class RoadGenerator {
    final RoadDensityChecker majorRoadDensityChecker;
    final RoadDensityChecker minorRoadDensityChecker;

    final TensorField2 field;
    final Vec2 bounds;
    final double minStepSize;
    final double maxStepSize;
    final double integrationTolerance;
    final int maxPoints;
    final double crossingDistance;
    final double minRoadLength;
    final double maxRoadLength;
    final double minGrowLength;
    final double proximityThreshold;

    RoadIntersectionFinder intersectionFinder;

    public ArrayList<Integer> fringeIndexes = new ArrayList<>();

    private RoadGenerator(Builder builder) {
        this.field = builder.field;
        this.bounds = builder.bounds;
        this.maxPoints = builder.maxPoints;
        this.minStepSize = builder.minStepSize;
        this.maxStepSize = builder.maxStepSize;
        this.integrationTolerance = builder.integrationTolerance;
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
        private double minStepSize = 0.1;
        private double maxStepSize = 1.0;
        private double integrationTolerance = 0.01;
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

        /// The minimum and maximum step sizes for road growth
        public Builder stepSize(double minStepSize, double maxStepSize) {
            this.minStepSize = minStepSize;
            this.maxStepSize = maxStepSize;
            return this;
        }

        public Builder integrationTolerance(double integrationTolerance) {
            this.integrationTolerance = integrationTolerance;
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

    public ArrayList<Path2> generateRoads(int initialSeedCount) {
        var roads = new ArrayList<Path2>();

        intersectionFinder = new RoadIntersectionFinder(roads, bounds, crossingDistance);

        var seeds = new PriorityQueue<RoadSeedWithPriority>();
        var pathId = 0;

        var completedSeeds = new ArrayList<RoadSeed>();

        for (int i = 0; i < initialSeedCount; i++) {
            var seed = RoadSeed.randomInBounds(bounds);
            seeds.add(new RoadSeedWithPriority(seed, 0));
        }

        while (!seeds.isEmpty()) {
            var queueItem = seeds.poll();
            var seed = queueItem.seed;

            var road = generateRoad(seed, pathId);
            if (road == null) {
                continue;
            }

            roads.add(road);

            completedSeeds.add(seed);

            intersectionFinder.add(road, pathId);

            (seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker).add(road);

            placeSeeds(road, seed.isMinorLine, seeds, queueItem.priority + 1);

            pathId++;
        }

        for (int i = 0; i < roads.size(); i++) {
            continueRoad(roads.get(i), i, completedSeeds.get(i));
        }

        // for (var tracer : tracers) {
        //     trimRoad(tracer);
        // }
        // // TODO Trim reversed roads? Find all dead ends exactly?

        // for (var tracer : tracers) {
        //     // TODO use a clearer constant for minimal point size of a trimmed road
        //     if (tracer.path.size() < Math.ceil(minRoadLength)) {
        //         continue;
        //     }

        //     roads.add(tracer.path);
        // }

        return roads;
    }

    /// @return null if failed to generate a road (e.g. if the road is too short)
    Path2 generateRoad(RoadSeed seed, int pathId) {
        final var path = new Path2();

        final var integrator =
                new FieldIntegrator2(
                        field,
                        seed.isMinorLine,
                        minStepSize,
                        maxStepSize,
                        integrationTolerance,
                        seed.point,
                        seed.growDirection);

        final var roadDensityChecker =
                seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker;

        var totalLength = 0.0;
        var wentFarFromSeed = false;

        for (int points = 0; points < maxPoints; points++) {
            integrator.calculateNextPoint();

            var point = integrator.nextPoint();

            if (!point.isInBounds(bounds)) {
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed.point, proximityThreshold)) {
                wentFarFromSeed = true;
            } else if (wentFarFromSeed && point.isCloseTo(seed.point, proximityThreshold)) {
                path.close();
                break;
            }

            double growLength = integrator.nextDirection().length();

            if (growLength < minGrowLength) {
                break;
            }

            if (roadDensityChecker.checkTooDenseNear(point)
                    || totalLength + growLength >= maxRoadLength) {
                break;
            }

            path.add(point);
            totalLength += growLength;
        }

        if (totalLength < minRoadLength) {
            return null;
        }

        return path;
    }

    void continueRoad(Path2 road, int pathId, RoadSeed seed) {
        fringeIndexes.add(road.size() - 1);

        if (road.isClosed()) {
            return;
        }

        var totalGrowLength = 0.0;

        final var initialSize = road.size();

        final var integrator =
                new FieldIntegrator2(
                        field,
                        seed.isMinorLine,
                        minStepSize,
                        maxStepSize,
                        integrationTolerance,
                        road.getLastPoint(),
                        road.getLastSegmentDirection());

        for (; ; ) {
            integrator.calculateNextPoint();
            final var point = integrator.nextPoint();

            if (point == null) {
                break;
            }

            if (!point.isInBounds(bounds)) {
                break;
            }

            var growLength = integrator.nextDirection().length();

            if (growLength < minGrowLength) {
                break;
            }

            if (totalGrowLength + growLength >= crossingDistance) {
                break;
            }

            totalGrowLength += growLength;

            if (point.isCloseTo(seed.point, proximityThreshold)) {
                road.close();
                return;
            }

            var intersections = new ArrayList<RoadIntersectionFinder.Intersection>();
            intersectionFinder.findIntersections(pathId, road.getLastPoint(), point, intersections);

            if (!intersections.isEmpty()) {
                var intersection = intersections.get(intersections.size() - 1);
                road.add(intersection.point());
                return;
            }

            road.add(point);
        }

        road.size(initialSize);
    }

    private void placeSeeds(
            Path2 road,
            boolean isMinorLine,
            PriorityQueue<RoadSeedWithPriority> seeds,
            int priority) {

        var length = 0.0;

        for (int i = 0; i < road.segmentCount(); i++) {

            if (length <= 0) {
                length = crossingDistance;

                var seed = new RoadSeed(road.getPoint(i), !isMinorLine);
                seeds.add(new RoadSeedWithPriority(seed, priority));
            }

            length -= road.getSegmentLength(i);
        }
    }

    /*
    private void trimRoad(RoadTracer tracer) {
        var path = tracer.path;

        if (path.isClosed()) {
            return;
        }

        var intersection = intersectionFinder.findLastIntersection(path, tracer.pathId);

        if (intersection != null) {
            path.size(intersection.pointIndex() + 1);

            // TODO do we need this?
            // path.add(intersection.pointX(), intersection.pointY());

        } else {
            path.size(0);
        }
    }
        */

    static final class RoadSeedWithPriority implements Comparable<RoadSeedWithPriority> {
        final RoadSeed seed;

        /// Low priority = grows later.
        /// High priority = grows sooner.
        final int priority;

        RoadSeedWithPriority(RoadSeed seed, int priority) {
            this.seed = seed;
            this.priority = priority;
        }

        @Override
        public int compareTo(RoadSeedWithPriority other) {
            return -Integer.compare(priority, other.priority);
        }
    }
}
