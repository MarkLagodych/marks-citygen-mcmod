package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.RasterPath2;

final class RoadIntersectionFinder {
    /// The square "radius" of the area around a point that is checked for intersections.
    private static final int RADIUS = 1;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = RADIUS;

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

    public ArrayList<DirectedCrossing> findDirectedCrossings(
            ArrayList<RasterPath2> paths, int pathId) {

        var path = paths.get(pathId);
        var directedCrossings = new ArrayList<DirectedCrossing>();

        for (int i = 0; i < path.size(); i++) {
            var x = path.getX(i);
            var y = path.getY(i);

            var intersection = getIntersectionNear(x, y, i, pathId);

            if (intersection != null) {
                var directedCrossing =
                        DirectedCrossing.find(
                                path,
                                i,
                                paths.get(intersection.intersectedPathId()),
                                intersection.intersectedPathId(),
                                intersection.intersectedPointId());

                directedCrossings.add(directedCrossing);
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
    record DirectedCrossing(
            int intersectedPathId, int intersectedPointId, boolean intersectedPathGoesLeft) {

        static DirectedCrossing find(
                RasterPath2 path,
                int pointIndex,
                RasterPath2 intersectedPath,
                int intersectedPathId,
                int intersectedPointIndex) {

            if (path.segmentCount() == 0 || intersectedPath.segmentCount() == 0) {
                throw new IllegalArgumentException("Paths must have at least 1 segment");
            }

            var baseSegmentIndex = Math.max(pointIndex - 1, 0);

            var intersectedSegmentIndex =
                    Math.min(intersectedPointIndex, intersectedPath.segmentCount() - 1);

            var baseSegment = path.getDirectionAt(baseSegmentIndex);
            var intersectedSegment = intersectedPath.getDirectionAt(intersectedSegmentIndex);

            var goesLeft = intersectedSegment.looksLeftOf(baseSegment);

            return new DirectedCrossing(intersectedPathId, intersectedPointIndex, goesLeft);
        }
    }
}
