package org.markscitygen.lib;

import java.util.ArrayList;
import java.util.PriorityQueue;
import org.markscitygen.lib.core.RasterPath2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;
import org.markscitygen.lib.tensorfield.TensorField2;

public final class RoadGenerator {
    final ArrayList<RasterPath2> roads = new ArrayList<>();
    final RoadDensityChecker majorRoadDensityChecker;
    final RoadDensityChecker minorRoadDensityChecker;

    final FieldIntegrator2 fieldIntegrator;

    final RoadIntersectionFinder intersectionFinder;

    final TensorField2 field;
    final Vec2 bounds;
    final double stepSize;
    final int maxPoints;
    final double crossingDistance;
    final double minRoadLength;
    final double maxRoadLength;
    final double minGrowLength;
    final double proximityThreshold;

    int nextPathId = 0;

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
        this.intersectionFinder =
                new RoadIntersectionFinder((int) Math.ceil(bounds.x), (int) Math.ceil(bounds.y));

        this.fieldIntegrator = new FieldIntegrator2(field, stepSize);
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

    public ArrayList<RasterPath2> getGeneratedRoads() {
        return roads;
    }

    public void generateRoads(int numRoads) {
        var seeds = new PriorityQueue<RoadSeedWithPriority>();
        var tracers = new ArrayList<RoadTracer>();

        for (int i = 0; i < numRoads; i++) {
            var seed = RoadSeed.randomInBounds(bounds, nextPathId++);
            seeds.add(new RoadSeedWithPriority(seed, 0));
        }

        while (!seeds.isEmpty()) {
            var queueItem = seeds.poll();
            var seed = queueItem.seed;

            var tracer = generateRoad(seed);
            if (tracer == null) {
                continue;
            }

            tracers.add(tracer);

            intersectionFinder.add(tracer.path, tracer.pathId);

            (seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker).add(tracer.path);

            placeSeeds(tracer.path, seed.isMinorLine, seeds, queueItem.priority + 1);
        }

        for (var tracer : tracers) {
            continueRoad(tracer);
        }

        for (var tracer : tracers) {
            trimRoad(tracer);
        }

        for (var tracer : tracers) {
            // TODO use a clearer constant for minimal point size of a trimmed road
            if (tracer.path.size() < Math.ceil(minRoadLength)) continue;

            roads.add(tracer.path);
        }
    }

    /// @return null if failed to generate a road (e.g. if the road is too short)
    RoadTracer generateRoad(RoadSeed seed) {
        final var tracer = new RoadTracer(seed, fieldIntegrator, minGrowLength);

        final var roadDensityChecker =
                seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker;

        var totalLength = 0.0;
        var wentFarFromSeed = false;

        for (int points = 0; points < maxPoints; points++) {
            final var point = tracer.nextPoint();

            if (point == null) {
                break;
            }

            if (!point.isInBounds(bounds)) {
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed.point, proximityThreshold)) {
                wentFarFromSeed = true;
            } else if (wentFarFromSeed && point.isCloseTo(seed.point, proximityThreshold)) {
                tracer.acceptPoint();
                tracer.path.close();
                break;
            }

            if (roadDensityChecker.checkTooDenseNear(point)
                    || totalLength + tracer.growLength >= maxRoadLength) {
                break;
            }

            tracer.acceptPoint();
            totalLength += tracer.growLength;
        }

        if (totalLength < minRoadLength) {
            return null;
        }

        return tracer;
    }

    void continueRoad(RoadTracer tracer) {
        var totalGrowLength = 0.0;

        final var initialSize = tracer.path.size();

        if (tracer.path.isClosed()) {
            return;
        }

        for (; ; ) {
            final var point = tracer.nextPoint();

            if (point == null) {
                break;
            }

            if (!point.isInBounds(bounds)) {
                break;
            }

            if (totalGrowLength + tracer.growLength >= crossingDistance) {
                break;
            }

            totalGrowLength += tracer.growLength;

            // This is also the index of the first point generated in this iteration
            final var sizeBeforeAdd = tracer.path.size();

            tracer.acceptPoint();

            if (point.isCloseTo(tracer.seedPoint, proximityThreshold)) {
                tracer.path.close();
                return;
            }

            var intersection =
                    intersectionFinder.findFirstIntersection(
                            tracer.path, sizeBeforeAdd, tracer.pathId);

            if (intersection != null) {
                tracer.path.size(intersection.pointIndex() + 1);
                tracer.path.add(intersection.pointX(), intersection.pointY());
                return;
            }
        }

        tracer.path.size(initialSize);
    }

    private void placeSeeds(
            RasterPath2 road,
            boolean isMinorLine,
            PriorityQueue<RoadSeedWithPriority> seeds,
            int priority) {

        var firstSeed =
                new RoadSeed(road.getPoint(0), !isMinorLine, road.getDirectionAt(0), nextPathId++);

        seeds.add(new RoadSeedWithPriority(firstSeed, priority));

        var length = 0.0;
        for (int i = 0; i < road.segmentCount(); i++) {
            length += road.getSegmentLength(i);

            if (length > crossingDistance) {
                length = 0.0;

                var seed =
                        new RoadSeed(
                                road.getPoint(i),
                                !isMinorLine,
                                road.getDirectionAt(i),
                                nextPathId++);

                seeds.add(new RoadSeedWithPriority(seed, priority));
            }
        }
    }

    private void trimRoad(RoadTracer tracer) {
        var path = tracer.path;

        if (path.isClosed()) {
            return;
        }

        var intersection = intersectionFinder.findLastIntersection(path, tracer.pathId);

        if (intersection != null) {
            path.size(intersection.pointIndex() + 1);
            path.add(intersection.pointX(), intersection.pointY());
        } else {
            path.size(0);
        }
    }

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
