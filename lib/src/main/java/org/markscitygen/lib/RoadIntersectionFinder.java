package org.markscitygen.lib;

import java.util.ArrayList;
import org.markscitygen.lib.core.Path2;
import org.markscitygen.lib.core.Vec2;

class RoadIntersectionFinder {
    /// TODO Why
    private static final double CROSSING_DISTANCE_TO_CELL_SIZE = 1.0 / 3;

    /// This many cells are added to each side of the bounding box to avoid edge cases
    /// where a point is near the edge of the bounding box.
    private static final int CELL_PADDING = 1;

    private final double coordToCellIndex;

    private final int rows;
    private final int cols;

    private final ArrayList<Subpath>[][] cells;

    public RoadIntersectionFinder(Vec2 bounds, double crossingDistance) {
        var cellSize = crossingDistance * CROSSING_DISTANCE_TO_CELL_SIZE;
        this.coordToCellIndex = 1.0 / cellSize;

        this.rows = (int) Math.ceil(bounds.y * coordToCellIndex) + 2 * CELL_PADDING;
        this.cols = (int) Math.ceil(bounds.x * coordToCellIndex) + 2 * CELL_PADDING;

        @SuppressWarnings("unchecked")
        ArrayList<Subpath>[][] cells = new ArrayList[rows][cols];
        this.cells = cells;
    }

    private int getRow(double y) {
        return (int) Math.floor(y * coordToCellIndex) + CELL_PADDING;
    }

    private int getCol(double x) {
        return (int) Math.floor(x * coordToCellIndex) + CELL_PADDING;
    }

    public void add(Path2 path, int pathId) {
        if (path.size() < 2) {
            return;
        }

        var firstSegmentId = 0;
        var lastSegmentId = firstSegmentId;

        var prevRow = getRow(path.getY(0));
        var prevCol = getCol(path.getX(0));

        for (int i = 1; i < path.size(); i++) {
            var row = getRow(path.getY(i));
            var col = getCol(path.getX(i));

            if (row != prevRow || col != prevCol) {
                addSubpath(pathId, firstSegmentId, lastSegmentId, prevRow, prevCol);
                firstSegmentId = i - 1;
            }
            lastSegmentId = i - 1;
            prevRow = row;
            prevCol = col;
        }

        addSubpath(pathId, firstSegmentId, lastSegmentId, prevRow, prevCol);
    }

    private void addSubpath(int pathId, int firstSegmentId, int lastSegmentId, int row, int col) {
        if (cells[row][col] == null) {
            cells[row][col] = new ArrayList<>();
        }
        cells[row][col].add(new Subpath(pathId, firstSegmentId, lastSegmentId));
    }

    public Intersection findFirstIntersection(ArrayList<Path2> roads, Vec2 p0, Vec2 p1) {
        int p0Row = getRow(p0.y);
        int p0Col = getCol(p0.x);
        int p1Row = getRow(p1.y);
        int p1Col = getCol(p1.x);

        int minRow = Math.min(p0Row, p1Row) - 1;
        int maxRow = Math.max(p0Row, p1Row) - 1;
        int minCol = Math.min(p0Col, p1Col) + 1;
        int maxCol = Math.max(p0Col, p1Col) + 1;

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                var intersection = findIntersectionInCell(roads, r, c, p0, p1);
                if (intersection != null) {
                    return intersection;
                }
            }
        }
        return null;
    }

    private Intersection findIntersectionInCell(
            ArrayList<Path2> roads, int row, int col, Vec2 p0, Vec2 p1) {
        if (cells[row][col] == null) {
            return null;
        }

        for (var subpath : cells[row][col]) {
            var road = roads.get(subpath.pathId);

            if (subpath.checkIntersects(road, p0, p1)) {
                return new Intersection(p0, subpath.pathId, subpath.firstSegmentId);
            }
        }

        return null;
    }

    public static record Intersection(Vec2 point, int pathId, int segmentId) {}

    static class Subpath {
        public final int pathId;
        public final int firstSegmentId;
        public final int lastSegmentId;

        public Subpath(int pathId, int segmentId, int lastSegmentId) {
            this.pathId = pathId;
            this.firstSegmentId = segmentId;
            this.lastSegmentId = lastSegmentId;
        }

        public boolean checkIntersects(Path2 road, Vec2 p0, Vec2 p1) {
            var q0 = road.getPoint(firstSegmentId);
            for (int segmentId = firstSegmentId + 1; segmentId <= lastSegmentId; segmentId++) {
                var q1 = road.getPoint(segmentId);
                if (checkIntersects(p0, p1, q0, q1)) {
                    return true;
                }
                q0 = q1;
            }
            return false;
        }

        // FIXME refactor this vibe-coded slop (clarify names)
        private static boolean checkIntersects(Vec2 p0, Vec2 p1, Vec2 q0, Vec2 q1) {
            // 1. Optional Bounding Box Quick Rejection
            if (Math.max(p0.x, p1.x) < Math.min(q0.x, q1.x)
                    || Math.max(q0.x, q1.x) < Math.min(p0.x, p1.x)
                    || Math.max(p0.y, p1.y) < Math.min(q0.y, q1.y)
                    || Math.max(q0.y, q1.y) < Math.min(p0.y, p1.y)) {
                return false;
            }

            // 2. Compute orientations (cross products)
            double d1 = direction(q0, q1, p0);
            double d2 = direction(q0, q1, p1);
            double d3 = direction(p0, p1, q0);
            double d4 = direction(p0, p1, q1);

            // 3. General case: endpoints straddle each other
            if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
                    && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
                return true;
            }

            // 4. Special cases: Collinear or touching endpoints
            if (d1 == 0 && onSegment(q0, q1, p0)) return true;
            if (d2 == 0 && onSegment(q0, q1, p1)) return true;
            if (d3 == 0 && onSegment(p0, p1, q0)) return true;
            if (d4 == 0 && onSegment(p0, p1, q1)) return true;

            return false;
        }

        // 2D cross product equivalent using Vec2 fields
        private static double direction(Vec2 p0, Vec2 p1, Vec2 q0) {
            return (q0.x - p0.x) * (p1.y - p0.y) - (q0.y - p0.y) * (p1.x - p0.x);
        }

        // Checks if point p lies on segment (r, s) when collinear
        private static boolean onSegment(Vec2 r, Vec2 s, Vec2 p) {
            return p.x <= Math.max(r.x, s.x)
                    && p.x >= Math.min(r.x, s.x)
                    && p.y <= Math.max(r.y, s.y)
                    && p.y >= Math.min(r.y, s.y);
        }
    }
}
