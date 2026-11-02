package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.RasterPath2;

public final class RoadIntersectionFinder {
    /// The square "radius" of the area around a point that is checked for intersections.
    private static final int RADIUS = 1;

    private static final int NEIGHBOURHOOD_SIDE = 2 * RADIUS + 1;

    private static final int NEIGHBOURHOOD_SIZE = NEIGHBOURHOOD_SIDE * NEIGHBOURHOOD_SIDE;

    /// The distance which the finder ignores around a crossing to avoid reporting multiple
    /// intersections for the same crossing.
    private static final int COOLDOWN_DISTANCE = 5;

    /// To get a direction at a given point at index `i` on a rasterized path, the algorithm takes
    /// a vector between two points at indexs `i-DIRECTION_RADIUS` and `i+DIRECTION_RADIUS`.
    private static final int DIRECTION_RADIUS = 5;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = RADIUS + 1;

    private static final long EMPTY_CELL = 0;

    private final int rows;
    private final int cols;

    private final long[] cells;

    public RoadIntersectionFinder(int width, int height) {
        this.rows = height + 2 * CELL_PADDING;
        this.cols = width + 2 * CELL_PADDING;
        this.cells = new long[rows * cols];
    }

    private int cellIndex(int x, int y) {
        return (y + CELL_PADDING) * cols + (x + CELL_PADDING);
    }

    private long encodeCell(int pathId, int pointId) {
        return ((long) (pathId + 1) << 32) | pointId;
    }

    private int decodeCellPathId(long cellValue) {
        return (int) (cellValue >>> 32) - 1;
    }

    private int decodeCellSegmentId(long cellValue) {
        return (int) cellValue;
    }

    private long getCell(int x, int y) {
        return cells[cellIndex(x, y)];
    }

    private long setCell(int x, int y, long cellValue) {
        return cells[cellIndex(x, y)] = cellValue;
    }

    public void add(RasterPath2 path, int pathId) {
        var cellValue = encodeCell(pathId, 0);

        for (int i = 0; i < path.size(); i++) {
            var x = path.getX(i);
            var y = path.getY(i);

            setCell(x, y, cellValue++);
        }
    }

    /// @return null if no intersection found
    private Intersection getIntersectionNear(int pointX, int pointY, int pointIndex, int pathId) {

        for (int x = pointX - RADIUS; x <= pointX + RADIUS; x++) {
            for (int y = pointY - RADIUS; y <= pointY + RADIUS; y++) {
                var cellValue = getCell(x, y);

                if (cellValue != EMPTY_CELL) {
                    var cellPathId = decodeCellPathId(cellValue);

                    if (cellPathId == pathId) {
                        continue;
                    }

                    var cellPointId = decodeCellSegmentId(cellValue);
                    return new Intersection(x, y, pointIndex, cellPathId, cellPointId);
                }
            }
        }

        return null;
    }

    /// Lists all intersections with path IDs > given pathId.
    private void listUniqueIntersectionsNear(
            int pointX, int pointY, int pointIndex, int pathId, Intersection[] intersections) {

        var intersectionCount = 0;

        for (int x = pointX - RADIUS; x <= pointX + RADIUS; x++) {
            for (int y = pointY - RADIUS; y <= pointY + RADIUS; y++) {
                var cellValue = getCell(x, y);

                if (cellValue == EMPTY_CELL) {
                    continue;
                }

                var cellPathId = decodeCellPathId(cellValue);

                if (cellPathId <= pathId) {
                    continue;
                }

                var cellPointId = decodeCellSegmentId(cellValue);

                intersections[intersectionCount++] =
                        new Intersection(x, y, pointIndex, cellPathId, cellPointId);
            }
        }
    }

    /// @return null if no intersection found
    public Intersection findFirstIntersection(RasterPath2 path, int startIndex, int pathId) {
        for (int i = startIndex; i < path.size(); i++) {
            var x = path.getX(i);
            var y = path.getY(i);

            var intersection = getIntersectionNear(x, y, i, pathId);

            if (intersection != null) {
                return intersection;
            }
        }

        return null;
    }

    /// @return null if no intersection found
    public Intersection findLastIntersection(RasterPath2 path, int pathId) {
        for (int i = path.size() - 1; i >= 0; i--) {
            var x = path.getX(i);
            var y = path.getY(i);

            var intersection = getIntersectionNear(x, y, i, pathId);

            if (intersection != null) {
                return intersection;
            }
        }

        return null;
    }

    /// Finds all directed crossings with *new* paths (with path ID > given pathId)
    public ArrayList<DirectedCrossing> findUniqueDirectedCrossings(
            ArrayList<RasterPath2> paths, int pathId) {

        var path = paths.get(pathId);
        var directedCrossings = new ArrayList<DirectedCrossing>();

        var cooldown = 0;

        for (int pointId = 0; pointId < path.size(); pointId++) {
            if (cooldown > 0) {
                cooldown--;
                continue;
            }

            var x = path.getX(pointId);
            var y = path.getY(pointId);

            var intersections = new Intersection[NEIGHBOURHOOD_SIZE];
            listUniqueIntersectionsNear(x, y, pointId, pathId, intersections);

            outer:
            for (int i = 0; i < intersections.length; i++) {
                var intersection = intersections[i];

                if (intersection == null) {
                    break;
                }

                for (int j = 0; j < i; j++) {
                    if (intersections[j].intersectedPathId == intersection.intersectedPathId) {
                        continue outer;
                    }
                }

                var directedCrossing =
                        DirectedCrossing.find(
                                path,
                                pointId,
                                paths.get(intersection.intersectedPathId()),
                                intersection.intersectedPathId(),
                                intersection.intersectedPointId());

                directedCrossings.add(directedCrossing);

                cooldown = COOLDOWN_DISTANCE;
            }
        }

        return directedCrossings;
    }

    public static record Intersection(
            int pointX,
            int pointY,
            int pointIndex,
            int intersectedPathId,
            int intersectedPointId) {}

    /// @param intersectedPathGoesLeft True if the intersected path goes left from the intersection.
    /// "Goes" suggests path traversal in the direction of increasing point indexes.
    /// "Left" is relative to the direction of the path that is being intersected.
    public static record DirectedCrossing(
            int pointId,
            int intersectedPathId,
            int intersectedPointId,
            boolean intersectedPathGoesLeft) {

        static DirectedCrossing find(
                RasterPath2 path,
                int pointId,
                RasterPath2 intersectedPath,
                int intersectedPathId,
                int intersectedPointIndex) {

            if (path.segmentCount() == 0 || intersectedPath.segmentCount() == 0) {
                throw new IllegalArgumentException("Paths must have at least 1 segment");
            }

            var basePointId0 = Math.max(pointId - DIRECTION_RADIUS, 0);
            var basePointId1 = Math.min(pointId + DIRECTION_RADIUS, path.size() - 1);

            var intersectedPointId0 = Math.max(intersectedPointIndex - DIRECTION_RADIUS, 0);
            var intersectedPointId1 =
                    Math.min(intersectedPointIndex + DIRECTION_RADIUS, intersectedPath.size() - 1);

            var basePoint0 = path.getPoint(basePointId0);
            var basePoint1 = path.getPoint(basePointId1);

            var intersectedPoint0 = intersectedPath.getPoint(intersectedPointId0);
            var intersectedPoint1 = intersectedPath.getPoint(intersectedPointId1);

            var baseSegment = basePoint0.directionTo(basePoint1);
            var intersectedSegment = intersectedPoint0.directionTo(intersectedPoint1);

            var goesLeft = intersectedSegment.looksLeftOf(baseSegment);

            return new DirectedCrossing(
                    pointId, intersectedPathId, intersectedPointIndex, goesLeft);
        }

        public DirectedCrossing reverse(int thisPathId) {
            return new DirectedCrossing(
                    intersectedPointId, thisPathId, pointId, !intersectedPathGoesLeft);
        }
    }
}
