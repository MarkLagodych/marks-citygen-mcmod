package org.markscitygen.lib;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;
import org.markscitygen.lib.tensorfield.TensorField2;

public final class RoadGenerator {
    /// Used to slightly extend the road continuation segment to avoid numerical issues with
    // intersection detection
    private static final double CONTINUATION_EXTENSION_FACTOR = 1.0001;

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

    // TODO replace this with a plain Config object and reference it from RoadGenerator
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

        var seeds = new PriorityQueue<RoadSeed>();
        var nextRoadId = 0;

        var completedSeeds = new ArrayList<RoadSeed>();

        for (int i = 0; i < initialSeedCount; i++) {
            seeds.add(RoadSeed.randomInBounds(bounds));
        }

        while (!seeds.isEmpty()) {
            var seed = seeds.poll();

            var road = generateRoad(seed, nextRoadId);
            if (road == null) {
                continue;
            }

            roads.add(road);

            completedSeeds.add(seed);

            intersectionFinder.add(nextRoadId);

            (seed.isMinorLine() ? minorRoadDensityChecker : majorRoadDensityChecker).add(road);

            placeSeeds(road, seed.isMinorLine(), seeds, seed.priority() + 1);

            nextRoadId++;
        }

        for (int roadId = 0; roadId < roads.size(); roadId++) {
            var road = roads.get(roadId);
            var lastSegmentId = road.segmentCount() - 1;

            continueRoad(road, roadId, completedSeeds.get(roadId));

            intersectionFinder.add(roadId, lastSegmentId + 1);
        }

        return roads;
    }

    /// @return null if failed to generate a road (e.g. if the road is too short)
    Path2 generateRoad(RoadSeed seed, int roadId) {
        final var path = new Path2(seed.point());

        final var integrator =
                new FieldIntegrator2(
                        field,
                        seed.isMinorLine(),
                        minStepSize,
                        maxStepSize,
                        integrationTolerance,
                        seed.point(),
                        seed.growDirection());

        final var roadDensityChecker =
                seed.isMinorLine() ? minorRoadDensityChecker : majorRoadDensityChecker;

        var totalLength = 0.0;
        var wentFarFromSeed = false;

        for (int points = 0; points < maxPoints; points++) {
            integrator.calculateNextPoint();

            var point = integrator.nextPoint();

            if (!point.isInBounds(bounds)) {
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed.point(), proximityThreshold)) {
                wentFarFromSeed = true;
            } else if (wentFarFromSeed && point.isCloseTo(seed.point(), proximityThreshold)) {
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

    void continueRoad(Path2 road, int roadId, RoadSeed seed) {
        if (road.isClosed()) {
            return;
        }

        var totalGrowLength = 0.0;

        final var initialSize = road.size();

        final var integrator =
                new FieldIntegrator2(
                        field,
                        seed.isMinorLine(),
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

            if (point.isCloseTo(seed.point(), proximityThreshold)) {
                road.close();
                return;
            }

            var segmentId = road.segmentCount();
            var segmentOrigin = road.getLastPoint();

            var intersections = new ArrayList<RoadIntersectionFinder.Intersection>();
            intersectionFinder.findIntersections(
                    roadId, segmentId, segmentOrigin, point, intersections);

            if (!intersections.isEmpty()) {
                intersections.sort(
                        (a, b) ->
                                Double.compare(
                                        segmentOrigin.distanceTo(a.point()),
                                        segmentOrigin.distanceTo(b.point())));

                var p = intersections.get(0).point();

                p.sub(segmentOrigin);
                p.mul(CONTINUATION_EXTENSION_FACTOR);
                p.add(segmentOrigin);

                road.add(p);
                return;
            }

            road.add(point);
        }

        road.size(initialSize);
    }

    void placeSeeds(Path2 road, boolean isMinorLine, PriorityQueue<RoadSeed> seeds, int priority) {

        var length = 0.0;

        for (int i = 0; i < road.segmentCount(); i++) {

            if (length >= crossingDistance) {
                length = 0.0;

                seeds.add(new RoadSeed(road.getPoint(i), !isMinorLine, priority));
            }

            length += road.getSegmentLength(i);
        }
    }

    public RoadNet createRoadNet(ArrayList<Path2> roads) {
        // Intersection point -> Road ID
        var intersectionNeighbourhoods = new HashMap<Vec2, IntArrayList>(roads.size());

        // Road ID -> Intersections on that road
        var allIntersections =
                new ArrayList<ArrayList<RoadIntersectionFinder.Intersection>>(roads.size());

        for (int roadId = 0; roadId < roads.size(); roadId++) {
            allIntersections.add(new ArrayList<>());
        }

        for (int roadId = 0; roadId < roads.size(); roadId++) {
            var intersections = allIntersections.get(roadId);
            intersectionFinder.findAllIntersections(roadId, intersections);

            for (var intersection : intersections) {
                intersectionNeighbourhoods.put(intersection.point(), new IntArrayList());

                allIntersections.get(intersection.intersectedPathId()).add(intersection.reverse());
            }
        }

        for (int roadId = 0; roadId < roads.size(); roadId++) {
            var road = roads.get(roadId);
            var intersections = allIntersections.get(roadId);

            intersections.sort(
                    (a, b) -> {
                        int cmp = Integer.compare(a.segmentId(), b.segmentId());
                        if (cmp != 0) return cmp;

                        var segmentOrigin = road.getPoint(a.segmentId());
                        return Double.compare(
                                segmentOrigin.distanceSquaredTo(a.point()),
                                segmentOrigin.distanceSquaredTo(b.point()));
                    });
        }

        for (int roadId = 0; roadId < roads.size(); roadId++) {
            var road = roads.get(roadId);
            var intersections = allIntersections.get(roadId);

            for (var intersection : intersections.reversed()) {
                road.insert(intersection.segmentId() + 1, intersection.point());
            }
        }

        var atomicRoads = new ArrayList<Path2>(roads.size());

        for (var road : roads) {
            var startPointId = 0;
            for (int pointId = 1; pointId < road.size(); pointId++) {
                var point = road.getPoint(pointId);
                if (intersectionNeighbourhoods.containsKey(point) || pointId == road.size() - 1) {
                    atomicRoads.add(road.subpath(startPointId, pointId - startPointId + 1));
                    startPointId = pointId;
                }
            }
        }

        /*
        for (var atomicRoad : atomicRoads) {
            var startPoint = atomicRoad.getPoint(0);
            var endPoint = atomicRoad.getLastPoint();

            if (intersectionNeighbourhoods.containsKey(startPoint)) {
                intersectionNeighbourhoods.get(startPoint).add(atomicRoads.indexOf(atomicRoad));
            }
            if (intersectionNeighbourhoods.containsKey(endPoint)) {
                intersectionNeighbourhoods.get(endPoint).add(atomicRoads.indexOf(atomicRoad));
            }
        }

        for (var entry : intersectionNeighbourhoods.entrySet()) {
            var center = entry.getKey();
            var neighbours = entry.getValue();

            // TODO sort neighbours by angle around center and find out the left/right neighbours
        }
             */

        debugAtomicRoads = atomicRoads;

        return null; // TODO Add this later
    }

    public ArrayList<Path2> debugAtomicRoads; // FIXME remove this after debugging
}
