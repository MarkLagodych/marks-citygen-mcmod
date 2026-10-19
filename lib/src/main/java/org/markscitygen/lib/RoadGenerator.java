package org.markscitygen.lib;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;
import org.markscitygen.lib.tensorfield.FieldIntegrator2;
import org.markscitygen.lib.tensorfield.TensorField2;

public final class RoadGenerator {
    final ArrayList<Path2> roads = new ArrayList<>();
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
        this.intersectionFinder = new RoadIntersectionFinder(bounds, proximityThreshold);

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

    public ArrayList<Path2> getGeneratedRoads() {
        return roads;
    }

    public void generateRoads(int numRoads) {
        var seeds = new ArrayDeque<RoadSeed>();

        for (int i = 0; i < numRoads; i++) {
            var seed = RoadSeed.randomInBounds(bounds);
            seeds.add(seed);
        }

        while (!seeds.isEmpty()) {
            var seed = seeds.pollFirst();
            var road = generateRoad(seed);
            if (road == null) {
                continue;
            }

            roads.add(road);
            intersectionFinder.add(road, roads.size() - 1);

            (seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker).add(road);

            placeSeeds(road, seed.isMinorLine, seeds);
        }
    }

    /// @return null if failed to generate a road (e.g. if the road is too short)
    public Path2 generateRoad(RoadSeed seed) {
        var path = new Path2(seed.point);

        var totalLength = 0.0;
        var prevPoint = seed.point;
        var prevGrowDirection = seed.growDirection;
        var wentFarFromSeed = false;

        var roadDensityChecker =
                seed.isMinorLine ? minorRoadDensityChecker : majorRoadDensityChecker;

        var shouldFindIntersection = false;
        var intersectionFound = false;
        var mainPoints = 0;
        var mainLength = 0.0;

        for (int points = 0; points < maxPoints; points++) {
            var point =
                    fieldIntegrator.getNextPoint(prevPoint, seed.isMinorLine, prevGrowDirection);

            if (!point.isInBounds(bounds)) {
                break;
            }

            var growDirection = prevPoint.directionTo(point);

            var growLength = growDirection.length();

            if (growLength < minGrowLength) {
                // Hit a degenerate point in the tensor field, cannot grow anymore
                break;
            }

            if (!wentFarFromSeed && !point.isCloseTo(seed.point, proximityThreshold)) {
                wentFarFromSeed = true;
            } else if (wentFarFromSeed && point.isCloseTo(seed.point, proximityThreshold)) {
                path.close();
                path.add(point);
                break;
            }

            if (!shouldFindIntersection) {
                if (roadDensityChecker.checkTooDenseNear(point)
                        || totalLength + growLength >= maxRoadLength) {
                    shouldFindIntersection = true;
                    mainPoints = points;
                    mainLength = totalLength;
                }
            } else {
                if (totalLength + growLength >= mainLength + crossingDistance) {
                    break;
                }
            }

            path.add(point);

            if (shouldFindIntersection) {
                var intersection =
                        intersectionFinder.findFirstIntersection(roads, prevPoint, point);
                if (intersection != null) {
                    intersectionFound = true;
                    break;
                }
            }

            prevPoint = point;
            prevGrowDirection = growDirection;
            totalLength += growLength;
        }

        if (shouldFindIntersection && !intersectionFound) {
            path.size(mainPoints);
            totalLength = mainLength;
        }

        if (totalLength < minRoadLength) {
            return null;
        }

        return path;
    }

    private void placeSeeds(Path2 road, boolean isMinorLine, Collection<RoadSeed> seeds) {
        var length = 0.0;
        for (int i = 0; i < road.segmentCount(); i++) {
            length += road.getSegmentLength(i);

            if (length > crossingDistance) {
                length = 0.0;
                seeds.add(new RoadSeed(road.getPoint(i), !isMinorLine, road.getDirectionAt(i)));
            }
        }
    }
}
